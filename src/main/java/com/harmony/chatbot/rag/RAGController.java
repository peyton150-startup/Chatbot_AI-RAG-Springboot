package com.harmony.chatbot.rag;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class RAGController {

    private final RAGService ragService;

    public RAGController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(consumes = "text/plain", produces = "text/plain")
    public String chat(@RequestBody String question) {
        return ragService.ask(question);
    }
}
