package com.harmony.chatbot.rag;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;

import java.util.Collections;
import java.util.List;

public class RAGService {

    private final OpenAiService service;

    public RAGService() {
        // Get API key from Render environment variable
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable not set");
        }
        this.service = new OpenAiService(apiKey);
    }

    /**
     * Create embeddings for a single text string
     * @param text The text to embed
     * @return List<Double> embedding vector
     */
    public List<Double> createEmbedding(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .input(Collections.singletonList(text)) // wrap String in List
                .model("text-embedding-3-small")
                .build();

        List<Embedding> response = service.createEmbeddings(request).getData();
        return response.get(0).getEmbedding();
    }

    /**
     * Add a document (for example, to a local vector DB)
     * @param doc The document text
     */
    public void addDocument(String doc) {
        List<Double> embedding = createEmbedding(doc);
        // Here you would save the embedding to your vector store
        System.out.println("Embedding created for doc, length: " + embedding.size());
    }
}
