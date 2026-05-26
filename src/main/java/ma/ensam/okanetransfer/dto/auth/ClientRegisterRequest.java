package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientRegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 8, max = 120) String password,
        @NotNull Long countryId
) {
}
