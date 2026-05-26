package ma.ensam.okanetransfer.dto.auth;

import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;

public record LoginResponse(
        boolean twoFactorRequired,
        String temporaryToken,
        JwtResponse tokens,
        UserSummaryResponse user
) {
}
