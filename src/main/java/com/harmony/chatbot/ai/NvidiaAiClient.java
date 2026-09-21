package com.harmony.chatbot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NvidiaAiClient implements AiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final NvidiaAiProperties properties;

    @Autowired
    public NvidiaAiClient(ObjectMapper objectMapper, NvidiaAiProperties properties) {
        this(HttpClient.newBuilder()
                .connectTimeout(properties.getTimeout())
                .build(), objectMapper, properties);
    }

    NvidiaAiClient(HttpClient httpClient, ObjectMapper objectMapper, NvidiaAiProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        validateConfiguration();
    }

    @Override
    public double[] embedQuery(String text) {
        return embed(List.of(requireText(text)), "query").get(0);
    }

    @Override
    public List<double[]> embedPassages(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("At least one passage is required");
        }
        return embed(texts.stream().map(this::requireText).toList(), "passage");
    }

    @Override
    public String chat(List<AiMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("At least one chat message is required");
        }

        List<Map<String, String>> serializedMessages = messages.stream()
                .map(message -> Map.of("role", message.role(), "content", message.content()))
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getChatModel());
        body.put("messages", serializedMessages);
        body.put("temperature", 0.2);
        body.put("max_tokens", 512);

        JsonNode root = post("/chat/completions", body, properties.getChatModel());
        String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
        if (content.isEmpty()) {
            throw new AiProviderException("NVIDIA chat response did not contain message content");
        }
        return content;
    }

    private List<double[]> embed(List<String> texts, String inputType) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getEmbeddingModel());
        body.put("input", texts);
        body.put("input_type", inputType);
        body.put("encoding_format", "float");
        body.put("truncate", "END");

        JsonNode data = post("/embeddings", body, properties.getEmbeddingModel()).path("data");
        if (!data.isArray() || data.size() != texts.size()) {
            throw new AiProviderException("NVIDIA embedding response contained " + data.size()
                    + " vectors; expected " + texts.size());
        }

        List<double[]> ordered = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            ordered.add(null);
        }
        for (JsonNode item : data) {
            int index = item.path("index").asInt(-1);
            JsonNode embedding = item.path("embedding");
            if (index < 0 || index >= ordered.size() || ordered.get(index) != null || !embedding.isArray()) {
                throw new AiProviderException("NVIDIA embedding response contained an invalid index or vector");
            }
            if (embedding.size() != properties.getEmbeddingDimensions()) {
                throw new AiProviderException("NVIDIA embedding response had " + embedding.size()
                        + " dimensions; expected " + properties.getEmbeddingDimensions());
            }
            double[] vector = new double[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i).asDouble();
            }
            ordered.set(index, vector);
        }
        return List.copyOf(ordered);
    }

    private JsonNode post(String endpoint, Object body, String model) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder(endpointUri(endpoint))
                    .timeout(properties.getTimeout())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiProviderException("NVIDIA request for model " + model
                        + " failed with HTTP " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiProviderException("NVIDIA request for model " + model + " was interrupted", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new AiProviderException("NVIDIA request for model " + model + " failed", e);
        }
    }

    private URI endpointUri(String endpoint) {
        return URI.create(properties.getBaseUrl().replaceAll("/+$", "") + endpoint);
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding input must not be blank");
        }
        return text;
    }

    private void validateConfiguration() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("NVIDIA_API_KEY is not configured");
        }
        if (properties.getEmbeddingDimensions() <= 0) {
            throw new IllegalStateException("NVIDIA embedding dimensions must be positive");
        }
    }
}
