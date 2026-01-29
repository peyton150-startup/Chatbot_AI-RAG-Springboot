package com.harmony.chatbot.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class RAGTest {

    public static void main(String[] args) {
        // Start Spring Boot application context
        ApplicationContext context = SpringApplication.run(RAGTest.class, args);

        // Read OpenAI key from application.properties
        Environment env = context.getEnvironment();
        String openaiKey = env.getProperty("openai.api.key");

        // Initialize RAGService with the key
        RAGService ragService = new RAGService(openaiKey);

        // Add some sample documents
        ragService.addDocument("Who can perform Botox?", "Harmonya Aesthetics provides Botox and filler services.");
        ragService.addDocument("How do I book an appointment?", "Appointments can be booked online or via phone.");

        // Query example
        String response = ragService.query("Who can perform Botox?");
        System.out.println("Q: Who can perform Botox?");
        System.out.println("A: " + response);
    }
}
