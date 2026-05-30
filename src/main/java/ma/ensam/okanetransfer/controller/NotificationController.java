package ma.ensam.okanetransfer.controller;

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
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<NotificationResponse>> myNotifications(
            Authentication authentication,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(
                authentication.getName(),
                status,
                PageRequest.of(page, size)
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authentication.getName()));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> sendTest(@Valid @RequestBody NotificationTestRequest request) {
        return ResponseEntity.ok(notificationService.sendTestNotification(request));
    }

    @GetMapping("/preferences")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getPreferences(authentication.getName()));
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            Authentication authentication,
            @RequestBody NotificationPreferencesResponse request
    ) {
        return ResponseEntity.ok(notificationService.updatePreferences(authentication.getName(), request));
    }
}
