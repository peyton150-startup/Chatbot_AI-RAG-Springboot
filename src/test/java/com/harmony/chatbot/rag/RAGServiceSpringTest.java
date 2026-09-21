package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.chatbot.ai.AiClient;
import com.harmony.chatbot.ai.NvidiaAiProperties;
import com.harmony.chatbot.analytics.ChatLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RAGServiceSpringTest {

    @Test
    void springSelectsTheProductionConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getBeanFactory().registerSingleton("aiClient", mock(AiClient.class));
            context.getBeanFactory().registerSingleton("chatLogRepository", mock(ChatLogRepository.class));
            context.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
            context.getBeanFactory().registerSingleton("nvidiaAiProperties", new NvidiaAiProperties());
            context.register(RAGService.class);

            context.refresh();

            assertNotNull(context.getBean(RAGService.class));
        }
    }
}
