package com.harmony.chatbot.rag;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RAGService {

    private final OpenAiService service;

    public RAGService(@Value("${openai.api.key}") String apiKey) {
        this.service = new OpenAiService(apiKey);
    }

    /**
     * Create embeddings for a given text.
     * @param text the input text
     * @return a List<Double> vector
     */
    public List<Double> createEmbedding(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .input(text)
                .model("text-embedding-3-small")
                .build();

        List<Embedding> response = service.createEmbeddings(request).getData();
        return response.get(0).getEmbedding(); // return first embedding
    }
}
