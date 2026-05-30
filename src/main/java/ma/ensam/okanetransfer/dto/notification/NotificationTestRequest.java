package ma.ensam.okanetransfer.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.ensam.okanetransfer.enums.NotificationChannel;

public class NotificationTestRequest {

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Recipient user id is required")
    private Long recipientUserId;

    @NotBlank(message = "Message is required")
    private String message;

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
