package ma.ensam.okanetransfer.dto.auth;

import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.NotificationChannel;
import ma.ensam.okanetransfer.enums.OtpPurpose;

public record OtpRequest(
        @NotNull OtpPurpose purpose,
        @NotNull NotificationChannel channel,
        String temporaryToken
) {
}
