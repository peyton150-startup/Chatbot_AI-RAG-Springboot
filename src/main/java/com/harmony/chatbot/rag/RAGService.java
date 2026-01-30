package com.harmony.chatbot.rag;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RAGService {

    private final OpenAiService openAi;

    public RAGService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set");
        }

        this.openAi = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    /**
     * Main RAG entry point
     */
    public String ask(String question) {

        // 🔒 HARD-CODED CONTEXT (replace later with vectors)
        String context = """
                Harmony Aesthetics is located at 123 Main Street, Austin, Texas.
                The office offers Botox, fillers, and skincare treatments.
                Appointments can be booked online or by phone.
                """;

        System.out.println("🔥 RAG HIT: " + question);

        ChatMessage system = new ChatMessage(
                "system",
                "You are a retrieval-based assistant. Answer ONLY using the provided context. " +
                "If the answer is not in the context, reply exactly: NOT IN CONTEXT.\n\n" +
                "CONTEXT:\n" + context
        );

        ChatMessage user = new ChatMessage("user", question);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(system, user))
                .temperature(0.0)
                .build();

        ChatCompletionResult result = openAi.createChatCompletion(request);
System.out.println("Loaded page: " + page.getSlug());

        return result.getChoices().get(0).getMessage().getContent().trim();
    }
}
