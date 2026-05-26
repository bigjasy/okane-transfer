package ma.ensam.okanetransfer.service;

import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(
            AuditAction action,
            User actor,
            String entityType,
            String entityId,
            String ipAddress,
            String userAgent,
            String detailsJson
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        if (actor != null) {
            auditLog.setActorUserId(actor.getId());
            auditLog.setActorEmail(actor.getEmail());
        }
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setDetailsJson(detailsJson);
        auditLogRepository.save(auditLog);
    }
}
