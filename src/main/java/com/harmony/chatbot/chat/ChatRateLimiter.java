package com.harmony.chatbot.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

/** A small in-process guard against anonymous chat cost abuse. */
@Component
public class ChatRateLimiter {
    private static final long WINDOW_MILLIS = 60_000L;
    private final int limitPerMinute;
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public ChatRateLimiter(@Value("${app.chat.rate-limit-per-minute:20}") int limitPerMinute) {
        this(limitPerMinute, Clock.systemUTC());
    }

    ChatRateLimiter(int limitPerMinute, Clock clock) {
        this.limitPerMinute = Math.max(1, limitPerMinute);
        this.clock = clock;
    }

    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        Window window = windows.compute(clientId, (key, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_MILLIS) return new Window(now, 1);
            return new Window(current.startedAt, current.count + 1);
        });
        return window.count <= limitPerMinute;
    }

    private record Window(long startedAt, int count) { }
}
