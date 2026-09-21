package com.harmony.chatbot.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NvidiaAiClientSpringTest {

    @Test
    void springSelectsTheProductionConstructor() {
        NvidiaAiProperties properties = new NvidiaAiProperties();
        properties.setApiKey("test-key");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
            context.getBeanFactory().registerSingleton("nvidiaAiProperties", properties);
            context.register(NvidiaAiClient.class);

            context.refresh();

            assertNotNull(context.getBean(NvidiaAiClient.class));
        }
    }
}
