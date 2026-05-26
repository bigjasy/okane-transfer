package ma.ensam.okanetransfer.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import ma.ensam.okanetransfer.domain.security.RefreshToken;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createToken(User user, String ipAddress, String userAgent) {
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(REFRESH_TOKEN_TTL));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshToken validate(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is revoked or expired", HttpStatus.UNAUTHORIZED);
        }
        return refreshToken;
    }

    @Transactional
    public String rotateToken(String rawToken, String ipAddress, String userAgent) {
        RefreshToken currentToken = validate(rawToken);
        currentToken.revoke();
        refreshTokenRepository.save(currentToken);
        return createToken(currentToken.getUser(), ipAddress, userAgent);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        RefreshToken currentToken = validate(rawToken);
        currentToken.revoke();
        refreshTokenRepository.save(currentToken);
    }

    @Transactional
    public void revokeTokenForUser(String rawToken, User user) {
        RefreshToken currentToken = validate(rawToken);
        if (!currentToken.getUser().getId().equals(user.getId())) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token does not belong to the authenticated user", HttpStatus.UNAUTHORIZED);
        }
        currentToken.revoke();
        refreshTokenRepository.save(currentToken);
    }

    @Transactional
    public int revokeAll(User user) {
        var activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        activeTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeTokens);
        return activeTokens.size();
    }

    public long getRefreshTokenExpiresInSeconds() {
        return REFRESH_TOKEN_TTL.toSeconds();
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
