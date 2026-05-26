package ma.ensam.okanetransfer.dto.user;

import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.UserStatus;

public record UserStatusUpdateRequest(
        @NotNull UserStatus status,
        String reason
) {
}
