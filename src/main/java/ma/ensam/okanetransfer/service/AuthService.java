package ma.ensam.okanetransfer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import ma.ensam.okanetransfer.domain.security.RefreshToken;
import ma.ensam.okanetransfer.domain.user.Client;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.auth.ClientRegisterRequest;
import ma.ensam.okanetransfer.dto.auth.JwtResponse;
import ma.ensam.okanetransfer.dto.auth.LoginRequest;
import ma.ensam.okanetransfer.dto.auth.LoginResponse;
import ma.ensam.okanetransfer.dto.auth.OtpVerifyRequest;
import ma.ensam.okanetransfer.dto.auth.RefreshTokenRequest;
import ma.ensam.okanetransfer.dto.user.UserProfileResponse;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.NotificationChannel;
import ma.ensam.okanetransfer.enums.OtpPurpose;
import ma.ensam.okanetransfer.enums.UserStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.ClientRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import ma.ensam.okanetransfer.security.JwtService;
import ma.ensam.okanetransfer.security.RefreshTokenService;
import ma.ensam.okanetransfer.security.TwoFactorService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String PASSWORD_POLICY =
            "Password must contain at least 8 characters, one uppercase, one lowercase, one digit and one special character";

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TwoFactorService twoFactorService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            TwoFactorService twoFactorService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.twoFactorService = twoFactorService;
        this.auditService = auditService;
    }

    @Transactional
    public UserSummaryResponse registerClient(ClientRegisterRequest request, String ipAddress, String userAgent) {
        validatePassword(request.password());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email is already used", HttpStatus.CONFLICT);
        }

        Client client = new Client();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setEmail(request.email());
        client.setPhoneNumber(request.phoneNumber());
        client.setCountryId(request.countryId());
        client.setPasswordHash(passwordEncoder.encode(request.password()));
        client.setStatus(UserStatus.ACTIVE);
        Client saved = clientRepository.save(client);

        auditService.record(
                AuditAction.USER_CREATED,
                saved,
                "User",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"source\":\"self-service\"}"
        );
        return UserSummaryResponse.from(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> invalidCredentials());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        ensureCanAuthenticate(user);
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        auditService.record(AuditAction.LOGIN, user, "User", String.valueOf(user.getId()), ipAddress, userAgent, null);

        if (user.isTwoFactorEnabled()) {
            var challenge = twoFactorService.requestOtp(user, OtpPurpose.LOGIN_2FA, NotificationChannel.EMAIL);
            String temporaryToken = jwtService.generateTemporaryToken(user, OtpPurpose.LOGIN_2FA);
            return new LoginResponse(
                    true,
                    temporaryToken,
                    null,
                    UserSummaryResponse.from(user),
                    challenge.simulatedCode());
        }

        return new LoginResponse(false, null, issueTokens(user, ipAddress, userAgent), UserSummaryResponse.from(user));
    }

    @Transactional
    public LoginResponse verifyOtp(OtpVerifyRequest request, String ipAddress, String userAgent) {
        User user = userFromTemporaryToken(request);
        twoFactorService.verifyOtp(user, request.purpose(), request.otpCode());
        return new LoginResponse(false, null, issueTokens(user, ipAddress, userAgent), UserSummaryResponse.from(user));
    }

    @Transactional
    public JwtResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshToken currentToken = refreshTokenService.validate(request.refreshToken());
        String rotatedRefreshToken = refreshTokenService.rotateToken(request.refreshToken(), ipAddress, userAgent);
        String accessToken = jwtService.generateAccessToken(currentToken.getUser());
        return new JwtResponse(accessToken, rotatedRefreshToken, jwtService.getAccessTokenExpiresInSeconds());
    }

    @Transactional
    public void logout(String refreshToken, User actor, String ipAddress, String userAgent) {
        refreshTokenService.revokeTokenForUser(refreshToken, actor);
        auditService.record(AuditAction.LOGOUT, actor, "User", String.valueOf(actor.getId()), ipAddress, userAgent, null);
    }

    @Transactional
    public int logoutAll(User actor, String ipAddress, String userAgent) {
        int revokedTokens = refreshTokenService.revokeAll(actor);
        auditService.record(AuditAction.LOGOUT, actor, "User", String.valueOf(actor.getId()), ipAddress, userAgent, "{\"all\":true}");
        return revokedTokens;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(User actor) {
        return UserProfileResponse.from(actor);
    }

    @Transactional(readOnly = true)
    public User currentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private JwtResponse issueTokens(User user, String ipAddress, String userAgent) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createToken(user, ipAddress, userAgent);
        return new JwtResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiresInSeconds());
    }

    private User userFromTemporaryToken(OtpVerifyRequest request) {
        try {
            Claims claims = jwtService.parseClaims(request.temporaryToken());
            if (!"TEMPORARY".equals(claims.get("type", String.class))) {
                throw new BusinessException("INVALID_TEMPORARY_TOKEN", "Invalid temporary token", HttpStatus.UNAUTHORIZED);
            }
            if (!request.purpose().name().equals(claims.get("purpose", String.class))) {
                throw new BusinessException("INVALID_OTP_PURPOSE", "OTP purpose does not match token", HttpStatus.UNAUTHORIZED);
            }
            Number userId = claims.get("userId", Number.class);
            return userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException("INVALID_TEMPORARY_TOKEN", "Invalid temporary token", HttpStatus.UNAUTHORIZED);
        }
    }

    private void ensureCanAuthenticate(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("USER_NOT_ACTIVE", "User account is not active", HttpStatus.UNAUTHORIZED);
        }
    }

    private BusinessException invalidCredentials() {
        return new BusinessException("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    private void validatePassword(String password) {
        boolean valid = password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
        if (!valid) {
            throw new BusinessException("WEAK_PASSWORD", PASSWORD_POLICY, HttpStatus.BAD_REQUEST);
        }
    }
}
