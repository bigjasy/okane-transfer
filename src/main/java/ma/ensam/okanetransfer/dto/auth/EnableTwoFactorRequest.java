package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record EnableTwoFactorRequest(
        @NotBlank String otpCode
) {
}
