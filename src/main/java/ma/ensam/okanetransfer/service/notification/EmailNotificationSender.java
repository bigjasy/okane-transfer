package ma.ensam.okanetransfer.service.notification;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationSender {

    private static final Logger LOGGER = Logger.getLogger(EmailNotificationSender.class.getName());

    private final JavaMailSenderImpl mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public EmailNotificationSender(
            JavaMailSenderImpl mailSender,
            @Value("${notification.email.enabled:false}") boolean enabled,
            @Value("${mail.from:}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public boolean isConfigured() {
        return enabled
                && fromAddress != null && !fromAddress.isBlank()
                && mailSender.getHost() != null && !mailSender.getHost().isBlank()
                && mailSender.getUsername() != null && !mailSender.getUsername().isBlank()
                && mailSender.getPassword() != null && !mailSender.getPassword().isBlank();
    }

    public void send(String toEmail, String subject, String body) {
        if (!isConfigured()) {
            throw new IllegalStateException("Email delivery is not configured. Set notification.email.enabled=true and mail.* properties.");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            LOGGER.info(() -> "Email sent to " + maskEmail(toEmail) + " — subject: " + subject);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + maskEmail(toEmail), exception);
            throw new IllegalStateException("Unable to send email: " + exception.getMessage(), exception);
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}
