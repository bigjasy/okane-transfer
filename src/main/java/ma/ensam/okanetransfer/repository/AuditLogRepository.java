package ma.ensam.okanetransfer.repository;

import java.time.LocalDateTime;
import java.util.List;
import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUserId(Long actorUserId);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
