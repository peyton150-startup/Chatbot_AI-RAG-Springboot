package com.harmony.chatbot.chat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatRateLimiterSpringTest {

    @Test
    void springSelectsTheConfiguredConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("test", Map.of("app.chat.rate-limit-per-minute", "7")));
            context.register(ChatRateLimiter.class);

            context.refresh();

            ChatRateLimiter limiter = context.getBean(ChatRateLimiter.class);
            for (int attempt = 0; attempt < 7; attempt++) {
                assertTrue(limiter.tryAcquire("client"));
            }
            assertFalse(limiter.tryAcquire("client"));
        }
    }
}
