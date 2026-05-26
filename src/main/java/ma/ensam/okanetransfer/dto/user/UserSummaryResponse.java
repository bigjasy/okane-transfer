package ma.ensam.okanetransfer.dto.user;

import ma.ensam.okanetransfer.domain.user.Agent;
import ma.ensam.okanetransfer.domain.user.Manager;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.enums.UserStatus;

public record UserSummaryResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        UserStatus status,
        String agencyName
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                resolveAgencyName(user)
        );
    }

    private static String resolveAgencyName(User user) {
        if (user instanceof Manager manager && manager.getAgencyId() != null) {
            return "Agency #" + manager.getAgencyId();
        }
        if (user instanceof Agent agent && agent.getAgencyId() != null) {
            return "Agency #" + agent.getAgencyId();
        }
        return null;
    }
}
