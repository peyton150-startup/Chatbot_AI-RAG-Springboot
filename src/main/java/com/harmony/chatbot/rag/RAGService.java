package com.harmony.chatbot.rag;

import org.springframework.stereotype.Service;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);

    private final OpenAiService service;
    private final List<String> docs;
    private final List<List<Double>> embeddings;

    public RAGService() {
        this.service = new OpenAiService(System.getenv("OPENAI_API_KEY"));

        // Example pages split into chunks
        docs = List.of(
            "Harmony Aesthetics & Wellness is a trusted medical spa located in Kensington, Maryland and Falls Church, Virginia, dedicated to helping clients look and feel their best through advanced aesthetic and wellness treatments.",
            "The Kensington office hours are Monday through Friday from 9:00 am to 5:00 pm, Saturday from 9:00 am to 3:00 pm by appointment only.",
            "The practice is run by Dr. Mario Ortega with board-certified NP Angelica and esthetician Alonnie.",
            "For appointments or questions, call or text (240) 280-0020 or book online at harmonyaestheticsandwellness.glossgenius.com."
        );

        embeddings = new ArrayList<>();
        for (String doc : docs) {
            EmbeddingResponse resp = service.createEmbeddings(
                EmbeddingRequest.builder()
                    .model("text-embedding-3-large")
                    .input(List.of(doc))
                    .build()
            );
            embeddings.add(resp.getData().get(0).getEmbedding());
            logger.info("Generated embedding for doc: {}", doc.substring(0, Math.min(50, doc.length())) + "...");
        }
    }

    public String getAnswer(String question) {
        logger.info("Received question: {}", question);

        EmbeddingResponse qEmb = service.createEmbeddings(
            EmbeddingRequest.builder()
                .model("text-embedding-3-large")
                .input(List.of(question))
                .build()
        );
        List<Double> qVector = qEmb.getData().get(0).getEmbedding();

        double bestScore = -1;
        String bestDoc = null;
        for (int i = 0; i < docs.size(); i++) {
            double sim = cosineSimilarity(qVector, embeddings.get(i));
            logger.info("Similarity with doc {}: {}", i, sim);
            if (sim > bestScore) {
                bestScore = sim;
                bestDoc = docs.get(i);
            }
        }

        if (bestDoc == null) return "I don’t have that information.";

        ChatMessage system = new ChatMessage("system", "Answer only using the following context: " + bestDoc);
        ChatMessage user = new ChatMessage("user", question);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(system, user))
                .build();

        String response = service.createChatCompletion(request)
                      .getChoices().get(0).getMessage().getContent();
        logger.info("Generated answer: {}", response);
        return response;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
