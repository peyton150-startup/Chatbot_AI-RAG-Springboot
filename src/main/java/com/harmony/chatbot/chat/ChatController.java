package com.harmony.chatbot.chat;

import com.harmony.chatbot.rag.RAGService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RAGService ragService;

    public ChatController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String question) {
        return ragService.getAnswer(question);
    }
}
