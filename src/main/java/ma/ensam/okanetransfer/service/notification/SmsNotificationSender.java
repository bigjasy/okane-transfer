package ma.ensam.okanetransfer.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationSender {

    private static final Logger LOGGER = Logger.getLogger(SmsNotificationSender.class.getName());

    private final boolean enabled;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SmsNotificationSender(
            @Value("${notification.sms.enabled:false}") boolean enabled,
            @Value("${twilio.account-sid:}") String accountSid,
            @Value("${twilio.auth-token:}") String authToken,
            @Value("${twilio.from-number:}") String fromNumber,
            ObjectMapper objectMapper
    ) {
        this.enabled = enabled;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigured() {
        return enabled
                && accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
    }

    public void send(String toPhoneNumber, String body) {
        if (!isConfigured()) {
            throw new IllegalStateException("SMS delivery is not configured. Set notification.sms.enabled=true and twilio.* properties.");
        }
        String normalizedTo = normalizePhoneNumber(toPhoneNumber);
        try {
            String formBody = "To=" + encode(normalizedTo)
                    + "&From=" + encode(fromNumber.trim())
                    + "&Body=" + encode(body);

            String credentials = accountSid + ":" + authToken;
            String authorization = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Twilio API error HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String sid = json.path("sid").asText("unknown");
            LOGGER.info(() -> "SMS sent to " + maskPhone(normalizedTo) + " — Twilio sid: " + sid);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to send SMS to " + maskPhone(toPhoneNumber), exception);
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to send SMS: " + exception.getMessage(), exception);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Recipient phone number is required for SMS delivery.");
        }
        String trimmed = phoneNumber.trim().replace(" ", "");
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        if (trimmed.startsWith("00")) {
            return "+" + trimmed.substring(2);
        }
        if (trimmed.startsWith("0")) {
            return "+212" + trimmed.substring(1);
        }
        return "+" + trimmed;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return phone.substring(0, Math.min(4, phone.length())) + "****";
    }
}
