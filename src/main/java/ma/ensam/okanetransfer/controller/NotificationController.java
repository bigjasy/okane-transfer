package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationPreferencesResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationTestRequest;
import ma.ensam.okanetransfer.enums.NotificationStatus;
import ma.ensam.okanetransfer.service.NotificationService;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app inbox plus real SMTP email and Twilio SMS when configured (M3)")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    @Operation(summary = "My notifications", description = "Paginated inbox for the authenticated user.")
    public ResponseEntity<PageResponse<NotificationResponse>> myNotifications(
            Authentication authentication,
            @RequestParam(name = "status", required = false) NotificationStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(
                authentication.getName(),
                status,
                PageRequest.of(page, size)
        ));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authentication.getName()));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send test notification", description = "Delivers a real email/SMS when configured, otherwise returns an error.")
    public ResponseEntity<NotificationResponse> sendTest(@Valid @RequestBody NotificationTestRequest request) {
        return ResponseEntity.ok(notificationService.sendTestNotification(request));
    }

    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getPreferences(authentication.getName()));
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            Authentication authentication,
            @RequestBody NotificationPreferencesResponse request
    ) {
        return ResponseEntity.ok(notificationService.updatePreferences(authentication.getName(), request));
    }
}
