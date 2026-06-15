package ma.ensam.okanetransfer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.NotificationRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import ma.ensam.okanetransfer.service.notification.EmailNotificationSender;
import ma.ensam.okanetransfer.service.notification.SmsNotificationSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailNotificationSender emailNotificationSender;
    private final SmsNotificationSender smsNotificationSender;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailNotificationSender emailNotificationSender,
            SmsNotificationSender smsNotificationSender
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailNotificationSender = emailNotificationSender;
        this.smsNotificationSender = smsNotificationSender;
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
                "OkaneTransfer test",
                request.getMessage(),
                "TestNotification",
                String.valueOf(recipient.getId()),
                true
        ));
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return toPreferences(user);
    }

    public NotificationPreferencesResponse updatePreferences(String email, NotificationPreferencesResponse request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        user.setNotifyEmailEnabled(request.isEmail());
        user.setNotifySmsEnabled(request.isSms());
        user.setNotifyPushEnabled(request.isPush());
        userRepository.save(user);
        return toPreferences(user);
    }

    public void notifyKycReview(User recipient, KycStatus status) {
        String message = status == KycStatus.APPROVED
                ? "Your KYC document has been approved."
                : "Your KYC document has been rejected. Please upload a new document or contact support.";
        createAndSend(recipient, NotificationChannel.IN_APP, "KYC update", message, "KycDocument", recipient.getEmail(), false);
        createAndSend(recipient, NotificationChannel.EMAIL, "KYC update", message, "KycDocument", recipient.getEmail(), false);
        createAndSend(recipient, NotificationChannel.SMS, "KYC update", message, "KycDocument", recipient.getEmail(), false);
    }

    public void notifyMobileMoneyWallet(
            String walletPhone,
            String transferReference,
            String operator,
            String operatorReference
    ) {
        if (walletPhone == null || walletPhone.isBlank()) {
            return;
        }
        String message = "OkaneTransfer: transfert " + transferReference
                + " envoyé vers " + operator + ". Réf. opérateur: " + operatorReference + ".";
        if (!smsNotificationSender.isConfigured()) {
            LOGGER.info(() -> "Mobile Money SMS (not configured) to " + walletPhone + ": " + message);
            return;
        }
        try {
            smsNotificationSender.send(walletPhone, message);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Mobile money wallet SMS failed for transfer " + transferReference, exception);
        }
    }

    public void notifyAmlAlert(Transfer transfer, List<AmlAlert> alerts) {
        String message = "Transfer " + transfer.getReference() + " blocked with " + alerts.size() + " AML alert(s).";
        userRepository.findByRole(Role.ROLE_ADMIN).forEach(admin -> {
            createAndSend(admin, NotificationChannel.IN_APP, "AML alert", message, "Transfer", transfer.getReference(), false);
            createAndSend(admin, NotificationChannel.EMAIL, "AML alert", message, "Transfer", transfer.getReference(), false);
            createAndSend(admin, NotificationChannel.SMS, "AML alert", message, "Transfer", transfer.getReference(), false);
        });
    }

    public void sendOtpCode(User recipient, NotificationChannel channel, String otpCode, String purposeLabel) {
        String subject = "OkaneTransfer verification code";
        String message = purposeLabel + " verification code: " + otpCode + ". Valid for 5 minutes. Do not share this code.";
        if (channel == NotificationChannel.EMAIL) {
            emailNotificationSender.send(recipient.getEmail(), subject, message);
            return;
        }
        if (channel == NotificationChannel.SMS) {
            smsNotificationSender.send(recipient.getPhoneNumber(), message);
            return;
        }
        throw new BusinessException("Unsupported OTP channel: " + channel);
    }

    public boolean isOtpChannelConfigured(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailNotificationSender.isConfigured();
            case SMS -> smsNotificationSender.isConfigured();
            default -> false;
        };
    }

    private Notification createAndSend(
            User recipient,
            NotificationChannel channel,
            String title,
            String message,
            String relatedEntityType,
            String relatedEntityId,
            boolean forceDelivery
    ) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);

        if (!forceDelivery && !isChannelEnabledForUser(recipient, channel)) {
            saved.setStatus(NotificationStatus.FAILED);
            return notificationRepository.save(saved);
        }

        if (channel == NotificationChannel.IN_APP) {
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
            return notificationRepository.save(saved);
        }

        if (channel == NotificationChannel.PUSH) {
            LOGGER.info(() -> "Push notifications are not implemented yet for user " + recipient.getEmail());
            saved.setStatus(NotificationStatus.FAILED);
            return notificationRepository.save(saved);
        }

        try {
            deliverExternal(recipient, channel, title, message);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Notification delivery failed on channel " + channel, exception);
            saved.setStatus(NotificationStatus.FAILED);
            saved = notificationRepository.save(saved);
            if (forceDelivery) {
                throw new BusinessException(
                        "NOTIFICATION_DELIVERY_FAILED",
                        exception.getMessage(),
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
                );
            }
            return saved;
        }
        return notificationRepository.save(saved);
    }

    private void deliverExternal(User recipient, NotificationChannel channel, String title, String message) {
        if (channel == NotificationChannel.EMAIL) {
            emailNotificationSender.send(recipient.getEmail(), title, message);
            return;
        }
        if (channel == NotificationChannel.SMS) {
            smsNotificationSender.send(recipient.getPhoneNumber(), title + " — " + message);
            return;
        }
        throw new BusinessException("Unsupported notification channel: " + channel);
    }

    private boolean isChannelEnabledForUser(User user, NotificationChannel channel) {
        return switch (channel) {
            case IN_APP -> true;
            case EMAIL -> user.isNotifyEmailEnabled();
            case SMS -> user.isNotifySmsEnabled();
            case PUSH -> user.isNotifyPushEnabled();
        };
    }

    private NotificationPreferencesResponse toPreferences(User user) {
        return new NotificationPreferencesResponse(
                user.isNotifyEmailEnabled(),
                user.isNotifySmsEnabled(),
                user.isNotifyPushEnabled()
        );
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
