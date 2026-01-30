package com.harmony.chatbot.rag;

import org.springframework.stereotype.Service;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResponse;
import com.theokanning.openai.completion.chat.*;

import java.util.*;

@Service
public class RAGService {

    private final OpenAiService service;
    private final List<String> docs;
    private final List<List<Double>> embeddings;

    public RAGService() {
        this.service = new OpenAiService(System.getenv("OPENAI_API_KEY"));

        // Example pages (split content into chunks)
        docs = List.of(
            "Harmony Aesthetics & Wellness is a trusted medical spa located in Kensington, Maryland and Falls Church, Virginia...",
            "The Kensington office hours are Monday through Friday from 9:00 am to 5:00 pm...",
            "The practice is run by Dr. Mario Ortega with board-certified NP Angelica and esthetician Alonnie...",
            "For appointments or questions, call or text (240) 280-0020 or book online at harmonyaestheticsandwellness.glossgenius.com."
        );

        // Precompute embeddings
        embeddings = new ArrayList<>();
        System.out.println("=== RAGService: Loading embeddings for " + docs.size() + " docs ===");
        for (int i = 0; i < docs.size(); i++) {
            String doc = docs.get(i);
            System.out.println("Embedding doc " + i + ": " + doc.substring(0, Math.min(50, doc.length())) + "...");
            EmbeddingResponse resp = service.createEmbeddings(
                EmbeddingRequest.builder()
                    .model("text-embedding-3-large")
                    .input(List.of(doc))
                    .build()
            );
            embeddings.add(resp.getData().get(0).getEmbedding());
        }
        System.out.println("=== Finished loading embeddings ===");
    }

    public String getAnswer(String question) {
        System.out.println("\n=== New question received: " + question + " ===");

        // Get embedding for user question
        EmbeddingResponse qEmb = service.createEmbeddings(
            EmbeddingRequest.builder()
                .model("text-embedding-3-large")
                .input(List.of(question))
                .build()
        );
        List<Double> qVector = qEmb.getData().get(0).getEmbedding();

        System.out.print("User embedding (first 10 dims): ");
        for (int i = 0; i < 10; i++) System.out.print(qVector.get(i) + ", ");
        System.out.println();

        // Find the closest doc
        double bestScore = -1;
        String bestDoc = null;
        for (int i = 0; i < docs.size(); i++) {
            double sim = cosineSimilarity(qVector, embeddings.get(i));
            System.out.println("Similarity to doc " + i + ": " + sim);
            if (sim > bestScore) {
                bestScore = sim;
                bestDoc = docs.get(i);
            }
        }

        if (bestDoc == null) {
            System.out.println("No relevant chunks found.");
            return "I don’t have that information in my knowledge base.";
        }

        System.out.println("Top doc chosen (score " + bestScore + "): " + bestDoc.substring(0, Math.min(100, bestDoc.length())) + "...");

        // Send to GPT with context
        ChatMessage system = new ChatMessage("system", "Answer only using the following context: " + bestDoc);
        ChatMessage user = new ChatMessage("user", question);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(system, user))
                .build();

        String answer = service.createChatCompletion(request)
                               .getChoices().get(0).getMessage().getContent();
        System.out.println("GPT Answer: " + answer);
        return answer;
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
