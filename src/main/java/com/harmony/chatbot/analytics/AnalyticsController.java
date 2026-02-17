package com.harmony.chatbot.analytics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final ChatLogRepository chatLogRepository;
    private final ChatLogService chatLogService;

    public AnalyticsController(ChatLogRepository chatLogRepository, ChatLogService chatLogService) {
        this.chatLogRepository = chatLogRepository;
        this.chatLogService = chatLogService;
    }

    /** Paginated log list — admin only */
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<ChatLogEntity> getLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatLogService.getRecentLogs(page, size);
    }

    /** Summary stats — admin only */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalQuestions",   chatLogService.getTotalCount());
        summary.put("positiveRatings",  chatLogRepository.countPositiveRatings());
        summary.put("negativeRatings",  chatLogRepository.countNegativeRatings());

        List<Map<String, Object>> topQuestions = chatLogService.getTopQuestions(10).stream().map(row -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("question", row[0]);
            e.put("count",    row[1]);
            return e;
        }).toList();
        summary.put("topQuestions", topQuestions);

        List<Map<String, Object>> perDay = chatLogService.getQuestionsPerDay().stream().map(row -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("date",  row[0] != null ? row[0].toString() : "");
            e.put("count", row[1]);
            return e;
        }).toList();
        summary.put("questionsPerDay", perDay);

        return summary;
    }

    /**
     * List of distinct session IDs (paginated) — for conversation thread list.
     * Admin only.
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<String> getSessions(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatLogRepository.findDistinctSessionIds(PageRequest.of(page, size));
    }

    /**
     * Full conversation thread for a session — admin only.
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ChatLogEntity> getThread(@PathVariable String sessionId) {
        return chatLogRepository.findBySessionIdOrderByAskedAtAsc(sessionId);
    }

    /**
     * Rate a message — called publicly from the chatbot widget.
     * POST /api/analytics/rate  { "id": 5, "rating": 1 }
     */
    @PostMapping("/rate")
    public ResponseEntity<String> rate(@RequestBody Map<String, Integer> body) {
        Long id = body.get("id") != null ? body.get("id").longValue() : null;
        Integer rating = body.get("rating");
        if (id == null || rating == null || (rating != 1 && rating != -1))
            return ResponseEntity.badRequest().body("Invalid");

        chatLogRepository.findById(id).ifPresent(log -> {
            log.setRating(rating);
            chatLogRepository.save(log);
        });
        return ResponseEntity.ok("ok");
    }
}
