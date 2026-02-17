package com.harmony.chatbot.chat;

import com.harmony.chatbot.analytics.ChatLogEntity;
import com.harmony.chatbot.analytics.ChatLogRepository;
import com.harmony.chatbot.analytics.ChatLogService;
import com.harmony.chatbot.rag.RAGService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RAGService ragService;
    private final ChatLogService chatLogService;
    private final ChatLogRepository chatLogRepository;

    public ChatController(RAGService ragService, ChatLogService chatLogService, ChatLogRepository chatLogRepository) {
        this.ragService = ragService;
        this.chatLogService = chatLogService;
        this.chatLogRepository = chatLogRepository;
    }

    /** Called by the new widget — sends JSON { "question": "..." } */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> chatJson(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        return process(body.getOrDefault("question", "").trim(), request);
    }

    /** Fallback — plain text body (old embed or direct calls) */
    @PostMapping(value = "/chat", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> chatText(
            @RequestBody String question,
            HttpServletRequest request) {
        return process(question.trim(), request);
    }

    private Map<String, Object> process(String question, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();

        // Pass sessionId into RAGService so it can load conversation history
        // for multi-turn memory before generating the answer.
        String answer = ragService.getAnswer(question, sessionId);

        ChatLogEntity log = new ChatLogEntity();
        log.setQuestion(question);
        log.setAnswer(answer);
        log.setSessionId(sessionId);
        ChatLogEntity saved = chatLogRepository.save(log);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", saved.getId());
        response.put("answer", answer);
        return response;
    }
}