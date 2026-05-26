package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record DisableTwoFactorRequest(
        @NotBlank String password,
        @NotBlank String otpCode
) {
}
