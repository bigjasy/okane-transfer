package ma.ensam.okanetransfer.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.audit.AuditLogResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.repository.AuditLogRepository;
import ma.ensam.okanetransfer.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class GlobalAuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public GlobalAuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public Page<AuditLogResponse> searchLogs(
            Long actorUserId,
            AuditAction action,
            String actionQuery,
            String entityType,
            String adminEmail,
            Pageable pageable
    ) {
        verifyAdminAccess(adminEmail);

        Page<AuditLog> logs;
        if (actorUserId != null) {
            logs = auditLogRepository.findByActorUserId(actorUserId, pageable);
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action, pageable);
        } else if (actionQuery != null && !actionQuery.isBlank()) {
            String needle = actionQuery.trim().toLowerCase(Locale.ROOT);
            List<AuditAction> matching = Arrays.stream(AuditAction.values())
                    .filter(candidate -> candidate.name().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
            logs = matching.isEmpty()
                    ? Page.empty(pageable)
                    : auditLogRepository.findByActionIn(matching, pageable);
        } else if (entityType != null && !entityType.isBlank()) {
            logs = auditLogRepository.findByEntityTypeIgnoreCase(entityType.trim(), pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        return logs.map(AuditLogResponse::from);
    }

    public AuditLogResponse getLogById(Long id, String adminEmail) {
        verifyAdminAccess(adminEmail);

        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Log d'audit introuvable."));

        return AuditLogResponse.from(log);
    }

    private void verifyAdminAccess(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));

        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenOperationException("L'accès à l'audit global est strictement réservé aux administrateurs.");
        }
    }
}
