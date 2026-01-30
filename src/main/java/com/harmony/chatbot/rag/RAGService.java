package com.harmony.chatbot.rag;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RAGService {

    private final OpenAiService openAiService;

    // Inject API key from Render / application.properties
    public RAGService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    /**
     * This is GROUNDED generation.
     * If the model answers outside this context, RAG is broken.
     */
    public String ask(String question) {

        // 🔒 Your "knowledge base" (replace later with vector search results)
        String context = """
                Company Information:
                - Company name: Harmony AI
                - Office location: 742 Evergreen Terrace, Springfield
                - Support email: support@harmony.ai
                """;

        ChatMessage system = new ChatMessage(
                "system",
                """
                You are a company assistant.
                Use ONLY the provided context to answer.
                If the answer is not in the context, say:
                "I don’t have that information."
                
                Context:
                """ + context
        );

        ChatMessage user = new ChatMessage("user", question);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(system, user))
                .temperature(0.0)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);

        return result.getChoices().get(0).getMessage().getContent();
    }
}
