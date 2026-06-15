package ma.ensam.okanetransfer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.audit.AuditLogResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.service.GlobalAuditService;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    private final GlobalAuditService globalAuditService;

    public AuditController(GlobalAuditService globalAuditService) {
        this.globalAuditService = globalAuditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getGlobalAuditLogs(
            @RequestParam(name = "actorUserId", required = false) Long actorUserId,
            @RequestParam(name = "action", required = false) AuditAction action,
            @RequestParam(name = "actionQuery", required = false) String actionQuery,
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails currentUser) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLogResponse> logs = globalAuditService.searchLogs(
                actorUserId,
                action,
                actionQuery,
                entityType,
                currentUser.getUsername(),
                pageable
        );
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponse> getAuditLogDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        return ResponseEntity.ok(globalAuditService.getLogById(id, currentUser.getUsername()));
    }
}
