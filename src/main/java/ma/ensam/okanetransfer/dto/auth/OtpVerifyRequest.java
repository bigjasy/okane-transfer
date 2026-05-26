package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.OtpPurpose;

public record OtpVerifyRequest(
        @NotBlank String temporaryToken,
        @NotBlank String otpCode,
        @NotNull OtpPurpose purpose
) {
}
