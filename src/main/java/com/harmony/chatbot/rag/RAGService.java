package com.harmony.chatbot.rag;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class RAGService {

    private final OpenAiService service;
    private final VectorStore vectorStore;

    public RAGService() {
        this.service = new OpenAiService(System.getenv("OPENAI_API_KEY"));

        // Load vectors.json from classpath
        try (InputStream is = new ClassPathResource("vectors.json").getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Page[] pages = objectMapper.readValue(is, Page[].class);

            // Initialize VectorStore with pages
            this.vectorStore = new VectorStore(pages);

            System.out.println("VectorStore loaded with " + pages.length + " pages.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load vectors.json", e);
        }
    }

    public String getAnswer(String question) {
        try {
            // Compute embedding for user question
            List<Embedding> qEmb = service.createEmbeddings(
                    EmbeddingRequest.builder()
                            .model("text-embedding-3-large")
                            .input(List.of(question))
                            .build()
            ).getData();

            double[] qVector = qEmb.get(0).getEmbedding().stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();

            // Retrieve the most relevant page from the vector store
            Page page = vectorStore.getMostRelevantPage(qVector);
            if (page == null) return "I don’t have that information.";

            // Send context + user question to GPT
            ChatMessage system = new ChatMessage("system",
                    "Answer only using the following context: " + page.getContent());
            ChatMessage user = new ChatMessage("user", question);

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(List.of(system, user))
                    .build();

            return service.createChatCompletion(request)
                    .getChoices().get(0)
                    .getMessage()
                    .getContent();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving answer.";
        }
    }
}
