package com.harmony.chatbot.leads;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_leads_email",   columnList = "email"),
        @Index(name = "idx_leads_session", columnList = "session_id")
})
public class LeadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String email;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "captured_at", nullable = false, updatable = false)
    private Instant capturedAt;

    @PrePersist
    public void prePersist() {
        if (capturedAt == null) capturedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Instant getCapturedAt() { return capturedAt; }
}
