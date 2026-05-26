package ma.ensam.okanetransfer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.auth.ClientRegisterRequest;
import ma.ensam.okanetransfer.dto.auth.JwtResponse;
import ma.ensam.okanetransfer.dto.auth.LoginRequest;
import ma.ensam.okanetransfer.dto.auth.LoginResponse;
import ma.ensam.okanetransfer.dto.auth.LogoutRequest;
import ma.ensam.okanetransfer.dto.auth.OtpVerifyRequest;
import ma.ensam.okanetransfer.dto.auth.RefreshTokenRequest;
import ma.ensam.okanetransfer.dto.user.UserProfileResponse;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;
import ma.ensam.okanetransfer.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-client")
    public ResponseEntity<UserSummaryResponse> registerClient(
            @Valid @RequestBody ClientRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        UserSummaryResponse response = authService.registerClient(
                request,
                clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/verify-otp")
    public LoginResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletRequest servletRequest) {
        return authService.verifyOtp(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        return authService.refresh(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest
    ) {
        User currentUser = authService.currentUser(authentication.getName());
        authService.logout(request.refreshToken(), currentUser, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
        return Map.of("loggedOut", true);
    }

    @PostMapping("/logout-all")
    public Map<String, Integer> logoutAll(Authentication authentication, HttpServletRequest servletRequest) {
        User currentUser = authService.currentUser(authentication.getName());
        int revokedTokens = authService.logoutAll(currentUser, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
        return Map.of("revokedTokens", revokedTokens);
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.me(authService.currentUser(authentication.getName()));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
