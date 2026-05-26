package ma.ensam.okanetransfer.dto.user;

import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.Role;

public record UserRoleUpdateRequest(@NotNull Role role) {
}
