package ma.ensam.okanetransfer.dto.user;

import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.Language;
import ma.ensam.okanetransfer.enums.Role;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        Language preferredLanguage
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getPreferredLanguage()
        );
    }
}
