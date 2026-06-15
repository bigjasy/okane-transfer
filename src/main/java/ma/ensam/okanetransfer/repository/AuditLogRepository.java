package ma.ensam.okanetransfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensam.okanetransfer.domain.audit.AuditLog;
import ma.ensam.okanetransfer.enums.AuditAction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByActorUserId(Long actorUserId, Pageable pageable);

    List<AuditLog> findByActorUserId(Long actorUserId);

    List<AuditLog> findByAction(AuditAction action);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByActionIn(List<AuditAction> actions, Pageable pageable);

    Page<AuditLog> findByEntityTypeIgnoreCase(String entityType, Pageable pageable);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}