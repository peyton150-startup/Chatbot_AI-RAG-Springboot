package com.harmony.chatbot;

import org.springframework.web.bind.annotation.*;

@RestController
public class ChatbotController {

    private final OpenAIService openAIService;

    public ChatbotController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return openAIService.askGPT(message);
    }
}
