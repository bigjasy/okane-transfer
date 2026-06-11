package ma.ensam.okanetransfer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotRequest;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotResponse processMessage(ChatbotRequest request) {
        String apiKey = resolveApiKey();
        System.err.println("[ChatbotService] API key resolved: " + (apiKey != null ? "set (" + apiKey.substring(0, Math.min(4, apiKey.length())) + "..." + ")" : "NOT SET"));
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[ChatbotService] GEMINI_API_KEY not set");
            return fallback("Configuration manquante : clé API Gemini non définie.");
        }

        try {
            String url = GEMINI_URL + "?key=" + apiKey;
            System.err.println("[ChatbotService] Calling Gemini API...");

            Map<String, Object> body = Map.of("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", buildPrompt(request.getMessage(), request.getLanguage()))
                    ))
            ));

            String jsonResponse = restTemplate.postForObject(url, body, String.class);
            System.err.println("[ChatbotService] Gemini response received: " + (jsonResponse != null ? jsonResponse.substring(0, Math.min(200, jsonResponse.length())) : "null"));
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            System.err.println("[ChatbotService] Gemini API call failed: " + e.getClass().getName() + " - " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return fallback("Le service IA est momentanément saturé (quota dépassé). Veuillez réessayer dans quelques instants.");
            }
            return fallback("Désolé, le service IA est temporairement indisponible. Veuillez réessayer plus tard.");
        }
    }

    private String buildPrompt(String message, String language) {
        return """
                Tu es l'assistant virtuel d'OkaneTransfer, une plateforme de transfert d'argent.
                Réponds de manière concise et utile aux questions sur les transferts, les frais,
                le suivi, les pays disponibles, etc.
                Si la question dépasse le cadre du service, propose d'escalader vers un agent humain.
                Langue de réponse : %s
                Question : %s
                """.formatted(language, message).stripIndent().strip();
    }

    private ChatbotResponse parseResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String text = root
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText("");

            String lower = text.toLowerCase();
            boolean escalated = lower.contains("escalade") || lower.contains("agent humain");
            String intent = escalated ? "ESCALATION" : "FAQ";

            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(text);
            response.setEscalated(escalated);
            response.setIntent(intent);
            return response;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            return fallback("Erreur lors de l'analyse de la réponse.");
        }
    }

    private ChatbotResponse fallback(String message) {
        ChatbotResponse response = new ChatbotResponse();
        response.setAnswer(message);
        response.setEscalated(false);
        response.setIntent("FALLBACK");
        return response;
    }

    private String resolveApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("gemini.api.key");
        }
        return key;
    }
}
