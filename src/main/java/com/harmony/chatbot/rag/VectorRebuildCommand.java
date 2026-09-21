package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.chatbot.ai.NvidiaAiClient;
import com.harmony.chatbot.ai.NvidiaAiProperties;

import java.nio.file.Path;

public final class VectorRebuildCommand {
    private static final int DEFAULT_BATCH_SIZE = 16;

    private VectorRebuildCommand() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: VectorRebuildCommand <input-json> <output-json>");
        }

        String apiKey = System.getenv("NVIDIA_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("NVIDIA_API_KEY is not configured");
        }

        NvidiaAiProperties properties = new NvidiaAiProperties();
        properties.setApiKey(apiKey);
        setIfPresent(System.getenv("NVIDIA_BASE_URL"), properties::setBaseUrl);
        setIfPresent(System.getenv("NVIDIA_EMBEDDING_MODEL"), properties::setEmbeddingModel);

        ObjectMapper objectMapper = new ObjectMapper();
        NvidiaAiClient client = new NvidiaAiClient(objectMapper, properties);
        new VectorFileRebuilder(client, objectMapper, DEFAULT_BATCH_SIZE)
                .rebuild(Path.of(args[0]), Path.of(args[1]));
        System.out.println("Vector file rebuilt successfully.");
    }

    private static void setIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }
}
