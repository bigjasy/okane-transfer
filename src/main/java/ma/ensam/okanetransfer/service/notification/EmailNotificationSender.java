package ma.ensam.okanetransfer.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String fromAddress;
    private final String provider;
    private final String sendgridApiKey;

    public EmailNotificationSender(
            JavaMailSenderImpl mailSender,
            ObjectMapper objectMapper,
            @Value("${notification.email.enabled:false}") boolean enabled,
            @Value("${mail.from:}") String fromAddress,
            @Value("${mail.provider:smtp}") String provider,
            @Value("${sendgrid.api.key:}") String sendgridApiKey
    ) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
        this.provider = provider == null ? "smtp" : provider.trim();
        this.sendgridApiKey = sendgridApiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public boolean isConfigured() {
        if (!enabled || fromAddress == null || fromAddress.isBlank()) {
            return false;
        }
        if (usesSendGrid()) {
            return sendgridApiKey != null && !sendgridApiKey.isBlank();
        }
        return mailSender.getHost() != null && !mailSender.getHost().isBlank()
                && mailSender.getUsername() != null && !mailSender.getUsername().isBlank()
                && mailSender.getPassword() != null && !mailSender.getPassword().isBlank();
    }

    public void send(String toEmail, String subject, String body) {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Email delivery is not configured. Set notification.email.enabled=true and mail.* or SendGrid properties."
            );
        }
        try {
            if (usesSendGrid()) {
                sendViaSendGrid(toEmail, subject, body);
            } else {
                sendViaSmtp(toEmail, subject, body);
            }
            LOGGER.info(() -> "Email sent to " + maskEmail(toEmail) + " — subject: " + subject);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + maskEmail(toEmail), exception);
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to send email: " + exception.getMessage(), exception);
        }
    }

    private boolean usesSendGrid() {
        return "sendgrid".equalsIgnoreCase(provider);
    }

    private void sendViaSmtp(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendViaSendGrid(String toEmail, String subject, String body) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode personalizations = root.putArray("personalizations");
        ObjectNode personalization = personalizations.addObject();
        ArrayNode to = personalization.putArray("to");
        to.addObject().put("email", toEmail.trim());

        ObjectNode from = root.putObject("from");
        from.put("email", extractEmail(fromAddress));
        String fromName = extractDisplayName(fromAddress);
        if (!fromName.isBlank()) {
            from.put("name", fromName);
        }

        root.put("subject", subject);
        ArrayNode content = root.putArray("content");
        content.addObject()
                .put("type", "text/plain")
                .put("value", body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + sendgridApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("SendGrid API error HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private String extractEmail(String from) {
        if (from == null) {
            return "";
        }
        int start = from.indexOf('<');
        int end = from.indexOf('>');
        if (start >= 0 && end > start) {
            return from.substring(start + 1, end).trim();
        }
        return from.trim();
    }

    private String extractDisplayName(String from) {
        if (from == null) {
            return "";
        }
        int start = from.indexOf('<');
        if (start > 0) {
            return from.substring(0, start).trim();
        }
        return "OkaneTransfer";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}
