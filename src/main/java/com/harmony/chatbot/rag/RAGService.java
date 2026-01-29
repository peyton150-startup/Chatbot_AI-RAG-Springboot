package com.harmony.chatbot.rag;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.List;

public class RAGService {

    private final OpenAiService service;
    private final List<String> documents = new ArrayList<>();

    public RAGService(String openaiKey) {
        this.service = new OpenAiService(openaiKey);
    }

    public void addDocument(String question, String answer) {
        documents.add(question + " " + answer);
    }

    public String query(String question) {
        for (String doc : documents) {
            if (doc.toLowerCase().contains(question.toLowerCase())) {
                return doc;
            }
        }
        return "No answer found.";
    }

    // Optional: create embeddings if needed
    public EmbeddingResponse createEmbedding(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .input(text)
                .model("text-embedding-3-small")
                .build();
        return service.createEmbeddings(request).getData().get(0);
    }
}
