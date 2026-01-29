package com.harmony.chatbot.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class RAGTest {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RAGTest.class, args);

        RAGService ragService = context.getBean(RAGService.class);

        String sampleText1 = "Who can perform Botox?";
        List<Double> embedding1 = ragService.createEmbedding(sampleText1);
        System.out.println("Embedding size for first question: " + embedding1.size());

        String sampleText2 = "How do I book an appointment?";
        List<Double> embedding2 = ragService.createEmbedding(sampleText2);
        System.out.println("Embedding size for second question: " + embedding2.size());
    }
}
