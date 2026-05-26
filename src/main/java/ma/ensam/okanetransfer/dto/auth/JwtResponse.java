package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record JwtResponse(
        @NotBlank String accessToken,
        @NotBlank String refreshToken,
        String tokenType,
        long expiresIn
) {
    public JwtResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
