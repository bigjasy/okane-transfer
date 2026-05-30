package ma.ensam.okanetransfer.repository;

import ma.ensam.okanetransfer.domain.notification.Notification;
import ma.ensam.okanetransfer.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndStatus(Long recipientId, NotificationStatus status, Pageable pageable);
}
