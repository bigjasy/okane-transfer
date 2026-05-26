package ma.ensam.okanetransfer.dto.audit;

import java.time.LocalDateTime;
import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.enums.AuditAction;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorEmail,
        AuditAction action,
        String entityType,
        String entityId,
        String ipAddress,
        String userAgent,
        String detailsJson,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getActorEmail(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getDetailsJson(),
                auditLog.getCreatedAt()
        );
    }
}
