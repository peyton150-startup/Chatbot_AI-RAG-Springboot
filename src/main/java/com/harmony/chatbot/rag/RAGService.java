package com.harmony.chatbot.rag;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.*;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private final OpenAiService openAiService;

    // Simple in-memory vector store
    private final List<PageEmbedding> vectorStore = new ArrayList<>();

    public RAGService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY not set");
        }

        this.openAiService = new OpenAiService(apiKey);

        // Load your documents once
        ingestDocument(
                "office",
                "Our office is located at 123 Main Street, New York City."
        );
    }

    /* ---------------- INGEST ---------------- */

    private void ingestDocument(String id, String content) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(List.of(content))
                .build();

        EmbeddingResult result = openAiService.createEmbeddings(request);
        List<Double> vector = result.getData().get(0).getEmbedding();

        vectorStore.add(new PageEmbedding(id, content, vector));
    }

    /* ---------------- ASK ---------------- */

    public String ask(String question) {

        // 1. Embed question
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(List.of(question))
                .build();

        EmbeddingResult embeddingResult =
                openAiService.createEmbeddings(embeddingRequest);

        List<Double> queryVector =
                embeddingResult.getData().get(0).getEmbedding();

        // 2. Similarity search
        List<PageEmbedding> topMatches = vectorStore.stream()
                .sorted((a, b) ->
                        Double.compare(
                                cosineSimilarity(queryVector, b.vector),
                                cosineSimilarity(queryVector, a.vector)))
                .limit(3)
                .collect(Collectors.toList());

        // 3. Inject context
        String context = topMatches.stream()
                .map(p -> p.content)
                .collect(Collectors.joining("\n"));

        // 4. Chat completion
        List<ChatMessage> messages = List.of(
                new ChatMessage("system",
                        "Answer using ONLY the context below.\n\n" + context),
                new ChatMessage("user", question)
        );

        ChatCompletionRequest chatRequest =
                ChatCompletionRequest.builder()
                        .model("gpt-4o-mini")
                        .messages(messages)
                        .build();

        ChatCompletionResult response =
                openAiService.createChatCompletion(chatRequest);

        return response.getChoices().get(0).getMessage().getContent();
    }

    /* ---------------- UTILS ---------------- */

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /* ---------------- MODEL ---------------- */

    private static class PageEmbedding {
        String id;
        String content;
        List<Double> vector;

        PageEmbedding(String id, String content, List<Double> vector) {
            this.id = id;
            this.content = content;
            this.vector = vector;
        }
    }
}
