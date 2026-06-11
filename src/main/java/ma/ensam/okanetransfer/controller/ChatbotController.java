package ma.ensam.okanetransfer.controller;

import jakarta.validation.Valid;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotRequest;
import ma.ensam.okanetransfer.dto.chatbot.ChatbotResponse;
import ma.ensam.okanetransfer.service.ChatbotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ChatbotResponse message(@Valid @RequestBody ChatbotRequest request) {
        return chatbotService.processMessage(request);
    }
}
