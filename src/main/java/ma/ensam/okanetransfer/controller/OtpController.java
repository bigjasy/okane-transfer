package ma.ensam.okanetransfer.controller;

import jakarta.validation.Valid;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.auth.DisableTwoFactorRequest;
import ma.ensam.okanetransfer.dto.auth.EnableTwoFactorRequest;
import ma.ensam.okanetransfer.dto.auth.OtpChallengeResponse;
import ma.ensam.okanetransfer.dto.auth.OtpCheckRequest;
import ma.ensam.okanetransfer.dto.auth.OtpRequest;
import ma.ensam.okanetransfer.dto.auth.OtpVerificationResponse;
import ma.ensam.okanetransfer.dto.auth.TwoFactorStatusResponse;
import ma.ensam.okanetransfer.service.AuthService;
import ma.ensam.okanetransfer.service.OtpService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;

    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @PostMapping("/request")
    public OtpChallengeResponse requestOtp(
            @Valid @RequestBody OtpRequest request,
            Authentication authentication
    ) {
        User user = resolveUser(authentication, request.temporaryToken(), request.purpose());
        return otpService.requestOtp(user, request.purpose(), request.channel());
    }

    @PostMapping("/verify")
    public OtpVerificationResponse verifyOtp(
            @Valid @RequestBody OtpCheckRequest request,
            Authentication authentication
    ) {
        User user = resolveUser(authentication, request.temporaryToken(), request.purpose());
        return new OtpVerificationResponse(otpService.verifyOtp(user, request.purpose(), request.otpCode()));
    }

    @PostMapping("/enable-2fa")
    public TwoFactorStatusResponse enableTwoFactor(
            @Valid @RequestBody EnableTwoFactorRequest request,
            Authentication authentication
    ) {
        User user = authService.currentUser(authentication.getName());
        return new TwoFactorStatusResponse(otpService.enableTwoFactor(user, request.otpCode()));
    }

    @PostMapping("/disable-2fa")
    public TwoFactorStatusResponse disableTwoFactor(
            @Valid @RequestBody DisableTwoFactorRequest request,
            Authentication authentication
    ) {
        User user = authService.currentUser(authentication.getName());
        return new TwoFactorStatusResponse(otpService.disableTwoFactor(user, request.password(), request.otpCode()));
    }

    private User resolveUser(Authentication authentication, String temporaryToken, ma.ensam.okanetransfer.enums.OtpPurpose purpose) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authService.currentUser(authentication.getName());
        }
        return otpService.userFromTemporaryToken(temporaryToken, purpose);
    }
}
