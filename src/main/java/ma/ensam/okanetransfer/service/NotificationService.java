package ma.ensam.okanetransfer.service;

import java.time.LocalDateTime;
import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.AmlAlert;
import ma.ensam.okanetransfer.domain.notification.Notification;
import ma.ensam.okanetransfer.domain.transfer.Transfer;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationPreferencesResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationResponse;
import ma.ensam.okanetransfer.dto.notification.NotificationTestRequest;
import ma.ensam.okanetransfer.enums.KycStatus;
import ma.ensam.okanetransfer.enums.NotificationChannel;
import ma.ensam.okanetransfer.enums.NotificationStatus;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.NotificationRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(
            String email,
            NotificationStatus status,
            Pageable pageable
    ) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        Page<Notification> page = status == null
                ? notificationRepository.findByRecipientId(user.getId(), pageable)
                : notificationRepository.findByRecipientIdAndStatus(user.getId(), status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public NotificationResponse markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Cannot read another user's notification");
        }
        notification.setStatus(NotificationStatus.READ);
        return toResponse(notificationRepository.save(notification));
    }

    public NotificationResponse sendTestNotification(NotificationTestRequest request) {
        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getRecipientUserId()));
        return toResponse(createAndSend(
                recipient,
                request.getChannel(),
                "Test notification",
                request.getMessage(),
                "TestNotification",
                String.valueOf(recipient.getId())
        ));
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(String email) {
        userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return new NotificationPreferencesResponse(true, true, false);
    }

    public NotificationPreferencesResponse updatePreferences(String email, NotificationPreferencesResponse request) {
        userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return request;
    }

    public void notifyKycReview(User recipient, KycStatus status) {
        String message = status == KycStatus.APPROVED
                ? "Your KYC document has been approved."
                : "Your KYC document has been rejected.";
        createAndSend(recipient, NotificationChannel.EMAIL, "KYC update", message, "KycDocument", recipient.getEmail());
    }

    public void notifyAmlAlert(Transfer transfer, List<AmlAlert> alerts) {
        userRepository.findByRole(Role.ROLE_ADMIN).forEach(admin ->
                createAndSend(
                        admin,
                        NotificationChannel.EMAIL,
                        "AML alert",
                        "Transfer " + transfer.getReference() + " blocked with " + alerts.size() + " alert(s).",
                        "Transfer",
                        transfer.getReference()
                )
        );
    }

    private Notification createAndSend(
            User recipient,
            NotificationChannel channel,
            String title,
            String message,
            String relatedEntityType,
            String relatedEntityId
    ) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setChannel(notification.getChannel());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
