package com.harmony.chatbot.rag;

import com.harmony.chatbot.ai.AiClient;
import com.harmony.chatbot.ai.AiMessage;
import com.harmony.chatbot.ai.AiProviderException;
import com.harmony.chatbot.analytics.ChatLogRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RAGServiceTest {

    @Test
    void retrievesContextAndSendsItWithTheQuestionToTheChatModel() {
        RecordingAiClient aiClient = new RecordingAiClient();
        aiClient.queryEmbedding = new double[]{1.0, 0.0};
        aiClient.chatAnswer = "We provide thirty-minute dog walks.";
        RAGService service = new RAGService(
                aiClient,
                mock(ChatLogRepository.class),
                new Page[]{page("walks", "Our standard dog walk lasts thirty minutes.", new double[]{1.0, 0.0})},
                2
        );

        String answer = service.getAnswer("How long is a dog walk?");

        assertEquals("We provide thirty-minute dog walks.", answer);
        assertEquals("How long is a dog walk?", aiClient.embeddedQuery);
        assertEquals("system", aiClient.chatMessages.get(0).role());
        assertTrue(aiClient.chatMessages.get(0).content().contains("Our standard dog walk lasts thirty minutes."));
        assertEquals(new AiMessage("user", "How long is a dog walk?"),
                aiClient.chatMessages.get(aiClient.chatMessages.size() - 1));
    }

    @Test
    void returnsTheSafeErrorMessageWhenTheProviderFails() {
        RecordingAiClient aiClient = new RecordingAiClient();
        aiClient.failure = new AiProviderException("provider unavailable");
        RAGService service = new RAGService(
                aiClient,
                mock(ChatLogRepository.class),
                new Page[]{page("walks", "Dog walks", new double[]{1.0, 0.0})},
                2
        );

        assertEquals("Error retrieving answer.", service.getAnswer("Question"));
    }

    private Page page(String id, String text, double[] embedding) {
        Page page = new Page();
        page.setId(id);
        page.setSource("source-" + id);
        page.setText(text);
        page.setEmbedding(embedding);
        return page;
    }

    private static final class RecordingAiClient implements AiClient {
        private double[] queryEmbedding;
        private String chatAnswer;
        private RuntimeException failure;
        private String embeddedQuery;
        private List<AiMessage> chatMessages = new ArrayList<>();

        @Override
        public double[] embedQuery(String text) {
            if (failure != null) {
                throw failure;
            }
            embeddedQuery = text;
            return queryEmbedding;
        }

        @Override
        public List<double[]> embedPassages(List<String> texts) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String chat(List<AiMessage> messages) {
            chatMessages = List.copyOf(messages);
            return chatAnswer;
        }
    }
}
