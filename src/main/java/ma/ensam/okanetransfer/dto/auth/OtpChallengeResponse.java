package ma.ensam.okanetransfer.dto.auth;

public record OtpChallengeResponse(
        Long otpId,
        long expiresInSeconds,
        String simulatedCode
) {
}
