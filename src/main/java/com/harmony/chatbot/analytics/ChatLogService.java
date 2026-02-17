package com.harmony.chatbot.analytics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLogService {

    private final ChatLogRepository chatLogRepository;

    public ChatLogService(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    /**
     * Save a question + answer to the database.
     */
    public void log(String question, String answer, String sessionId) {
        ChatLogEntity entry = new ChatLogEntity();
        entry.setQuestion(question);
        entry.setAnswer(answer);
        entry.setSessionId(sessionId);
        chatLogRepository.save(entry);
    }

    /**
     * Get paginated logs, most recent first.
     */
    public Page<ChatLogEntity> getRecentLogs(int page, int size) {
        return chatLogRepository.findAllByOrderByAskedAtDesc(PageRequest.of(page, size));
    }

    /**
     * Total number of questions ever asked.
     */
    public long getTotalCount() {
        return chatLogRepository.count();
    }

    /**
     * Top N most frequently asked questions.
     */
    public List<Object[]> getTopQuestions(int limit) {
        return chatLogRepository.findTopQuestions(PageRequest.of(0, limit));
    }

    /**
     * Questions asked per day (last 14 days).
     */
    public List<Object[]> getQuestionsPerDay() {
        return chatLogRepository.findQuestionsPerDay(PageRequest.of(0, 14));
    }
}
