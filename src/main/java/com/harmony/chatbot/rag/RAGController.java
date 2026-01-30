package com.harmony.chatbot.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class RAGController {

    @Autowired
    private RAGService ragService;

    @PostMapping
    public String chat(@RequestBody String question) {
        return ragService.getAnswer(question);
    }
}
