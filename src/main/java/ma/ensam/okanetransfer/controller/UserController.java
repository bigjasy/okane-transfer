package ma.ensam.okanetransfer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.audit.AuditLogResponse;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.user.UserCreateRequest;
import ma.ensam.okanetransfer.dto.user.UserProfileResponse;
import ma.ensam.okanetransfer.dto.user.UserRoleUpdateRequest;
import ma.ensam.okanetransfer.dto.user.UserStatusUpdateRequest;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;
import ma.ensam.okanetransfer.dto.user.UserUpdateRequest;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.enums.UserStatus;
import ma.ensam.okanetransfer.service.AuthService;
import ma.ensam.okanetransfer.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public PageResponse<UserSummaryResponse> listUsers(
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "agencyId", required = false) Long agencyId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)));
        return userService.listUsers(role, status, agencyId, pageable, currentUser(authentication));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<UserSummaryResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest
    ) {
        UserSummaryResponse response = userService.createInternalUser(
                request,
                currentUser(authentication),
                clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse getUser(@PathVariable("id") Long id, Authentication authentication) {
        return userService.getUser(id, currentUser(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        return userService.updateUser(id, request, currentUser(authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserSummaryResponse updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest
    ) {
        return userService.updateStatus(
                id,
                request,
                currentUser(authentication),
                clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserSummaryResponse updateRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRoleUpdateRequest request,
            Authentication authentication
    ) {
        return userService.updateRole(id, request.role(), currentUser(authentication));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<AuditLogResponse> auditLogs(@PathVariable("id") Long id, Authentication authentication) {
        return userService.auditLogs(id, currentUser(authentication));
    }

    private User currentUser(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
