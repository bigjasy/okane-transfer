package ma.ensam.okanetransfer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotRequest;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public ChatbotService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public ChatbotResponse processMessage(ChatbotRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback("Configuration manquante : clé API Gemini non définie.");
        }

        try {
            String url = GEMINI_URL + "?key=" + apiKey;

            Map<String, Object> body = Map.of("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", buildPrompt(request.getMessage(), request.getLanguage()))
                    ))
            ));

            String jsonResponse = restTemplate.postForObject(url, body, String.class);
            return parseResponse(jsonResponse);
        } catch (Exception e) {
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

}
