package com.harmony.chatbot.rag;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class RAGController {

    private final RAGService ragService;

    public RAGController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * POST /api/chat
     * Body: { "question": "your question here" }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {

        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Question cannot be empty"));
        }

        String answer = ragService.ask(request.getQuestion());
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    /* =======================
       Request / Response DTOs
       ======================= */

    public static class ChatRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    public static class ChatResponse {
        private String answer;

        public ChatResponse(String answer) {
            this.answer = answer;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
