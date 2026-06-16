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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String OPENROUTER_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public ChatbotService(
            @Value("${openrouter.api.key}") String apiKey,
            @Value("${openrouter.model:openrouter/free}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public ChatbotResponse processMessage(ChatbotRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback("Configuration manquante : clé API OpenRouter non définie.");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", buildSystemPrompt(request.getLanguage())),
                            Map.of("role", "user", "content", request.getMessage())
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String jsonResponse = restTemplate.postForObject(OPENROUTER_URL, new HttpEntity<>(body, headers), String.class);
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return fallback("Le service IA est momentanément saturé (quota dépassé). Veuillez réessayer dans quelques instants.");
            }
            log.error("OpenRouter API call failed", e);
            return fallback("Désolé, le service IA est temporairement indisponible. Veuillez réessayer plus tard.");
        }
    }

    private String buildSystemPrompt(String language) {
        return """
                Tu es l'assistant virtuel d'OkaneTransfer, une plateforme de transfert d'argent.
                Réponds de manière concise et utile aux questions sur les transferts, les frais,
                le suivi, les pays disponibles, etc.
                Si la question dépasse le cadre du service, propose d'escalader vers un agent humain.
                Réponds uniquement en texte brut, sans markdown, sans gras, sans listes à puces.
                Langue de réponse : %s
                """.formatted(language).stripIndent().strip();
    }

    private ChatbotResponse parseResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String text = root
                    .path("choices").get(0)
                    .path("message").path("content")
                    .asText("");

            String lower = text.toLowerCase();
            boolean escalated = lower.contains("escalade") || lower.contains("agent humain");
            String intent = escalated ? "ESCALATION" : "FAQ";

            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(text);
            response.setEscalated(escalated);
            response.setIntent(intent);
            return response;
        } catch (Exception e) {
            log.error("Failed to parse OpenRouter response", e);
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
