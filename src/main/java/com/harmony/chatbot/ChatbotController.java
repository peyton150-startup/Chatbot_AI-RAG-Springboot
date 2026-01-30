package com.harmony.chatbot;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.harmony.chatbot.rag.RAGService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RAGService ragService;

    public ChatController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return ragService.getAnswer(message);
    }
}
