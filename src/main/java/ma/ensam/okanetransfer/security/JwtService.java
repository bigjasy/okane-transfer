package ma.ensam.okanetransfer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.OtpPurpose;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String JWT_SECRET_ENV = "OKANE_JWT_SECRET";
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);
    private static final Duration TEMPORARY_TOKEN_TTL = Duration.ofMinutes(5);

    private final SecretKey signingKey;

    public JwtService() {
        this.signingKey = resolveSigningKey();
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("type", "ACCESS");
        return buildToken(user.getEmail(), claims, ACCESS_TOKEN_TTL);
    }

    public String generateTemporaryToken(User user, OtpPurpose purpose) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("purpose", purpose.name());
        claims.put("type", "TEMPORARY");
        return buildToken(user.getEmail(), claims, TEMPORARY_TOKEN_TTL);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            return userDetails.getUsername().equals(claims.getSubject())
                    && "ACCESS".equals(claims.get("type", String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public long getAccessTokenExpiresInSeconds() {
        return ACCESS_TOKEN_TTL.toSeconds();
    }

    private String buildToken(String subject, Map<String, Object> claims, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    private SecretKey resolveSigningKey() {
        String configuredSecret = System.getenv(JWT_SECRET_ENV);
        if (configuredSecret == null || configuredSecret.isBlank()) {
            return Jwts.SIG.HS256.key().build();
        }
        byte[] keyBytes = decodeSecret(configuredSecret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(JWT_SECRET_ENV + " must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String configuredSecret) {
        byte[] rawBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
        if (rawBytes.length >= 32) {
            return rawBytes;
        }
        try {
            return Base64.getDecoder().decode(configuredSecret);
        } catch (IllegalArgumentException ignored) {
            return rawBytes;
        }
    }
}
