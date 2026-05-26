package ma.ensam.okanetransfer.dto.user;

import jakarta.validation.constraints.NotBlank;
import ma.ensam.okanetransfer.enums.Language;

public record UserUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        Language preferredLanguage
) {
}
