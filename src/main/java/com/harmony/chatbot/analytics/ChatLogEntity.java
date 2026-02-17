package com.harmony.chatbot.analytics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_log", indexes = {
        @Index(name = "idx_chat_session", columnList = "session_id")
})
public class ChatLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "asked_at", nullable = false, updatable = false)
    private Instant askedAt;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0 CHECK (rating IN (-1, 0, 1))")
    private int rating = 0;

    @PrePersist
    public void prePersist() {
        if (askedAt == null) askedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public void setQuestion(String q) { this.question = q; }
    public String getAnswer() { return answer; }
    public void setAnswer(String a) { this.answer = a; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String s) { this.sessionId = s; }
    public Instant getAskedAt() { return askedAt; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
