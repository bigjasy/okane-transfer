package ma.ensam.okanetransfer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.auth.OtpChallengeResponse;
import ma.ensam.okanetransfer.enums.NotificationChannel;
import ma.ensam.okanetransfer.enums.OtpPurpose;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.UserRepository;
import ma.ensam.okanetransfer.security.JwtService;
import ma.ensam.okanetransfer.security.TwoFactorService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final PasswordEncoder passwordEncoder;

    public OtpService(
            UserRepository userRepository,
            JwtService jwtService,
            TwoFactorService twoFactorService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.twoFactorService = twoFactorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OtpChallengeResponse requestOtp(User user, OtpPurpose purpose, NotificationChannel channel) {
        TwoFactorService.OtpChallenge challenge = twoFactorService.requestOtp(user, purpose, channel);
        return new OtpChallengeResponse(challenge.otpId(), challenge.expiresInSeconds(), challenge.simulatedCode());
    }

    @Transactional
    public boolean verifyOtp(User user, OtpPurpose purpose, String otpCode) {
        return twoFactorService.verifyOtp(user, purpose, otpCode);
    }

    @Transactional
    public boolean enableTwoFactor(User user, String otpCode) {
        twoFactorService.verifyOtp(user, OtpPurpose.LOGIN_2FA, otpCode);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean disableTwoFactor(User user, String password, String otpCode) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid password", HttpStatus.UNAUTHORIZED);
        }
        twoFactorService.verifyOtp(user, OtpPurpose.LOGIN_2FA, otpCode);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        return false;
    }

    @Transactional(readOnly = true)
    public User userFromTemporaryToken(String temporaryToken, OtpPurpose expectedPurpose) {
        try {
            Claims claims = jwtService.parseClaims(temporaryToken);
            if (!"TEMPORARY".equals(claims.get("type", String.class))) {
                throw invalidTemporaryToken();
            }
            if (!expectedPurpose.name().equals(claims.get("purpose", String.class))) {
                throw new BusinessException("INVALID_OTP_PURPOSE", "OTP purpose does not match token", HttpStatus.UNAUTHORIZED);
            }
            Number userId = claims.get("userId", Number.class);
            return userRepository.findById(userId.longValue())
                    .orElseThrow(() -> invalidTemporaryToken());
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidTemporaryToken();
        }
    }

    private BusinessException invalidTemporaryToken() {
        return new BusinessException("INVALID_TEMPORARY_TOKEN", "Invalid temporary token", HttpStatus.UNAUTHORIZED);
    }
}
