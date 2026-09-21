package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.chatbot.ai.AiClient;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class VectorFileRebuilder {
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    private final int batchSize;

    public VectorFileRebuilder(AiClient aiClient, ObjectMapper objectMapper, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
    }

    public void rebuild(Path input, Path output) throws IOException {
        Page[] pages = objectMapper.readValue(input.toFile(), Page[].class);
        if (pages == null || pages.length == 0) {
            throw new IllegalArgumentException("Vector source must contain at least one page");
        }

        List<String> texts = new ArrayList<>(pages.length);
        for (int i = 0; i < pages.length; i++) {
            Page page = pages[i];
            String id = page.getId() != null ? page.getId() : Integer.toString(i);
            if (page.getText() == null || page.getText().isBlank()) {
                throw new IllegalArgumentException("Page " + id + " has blank text");
            }
            texts.add(page.getText());
        }

        for (int start = 0; start < texts.size(); start += batchSize) {
            int end = Math.min(start + batchSize, texts.size());
            List<double[]> embeddings = aiClient.embedPassages(texts.subList(start, end));
            if (embeddings.size() != end - start) {
                throw new IllegalStateException("Embedding provider returned " + embeddings.size()
                        + " vectors for a batch of " + (end - start));
            }
            for (int offset = 0; offset < embeddings.size(); offset++) {
                pages[start + offset].setEmbedding(embeddings.get(offset));
            }
        }

        Path absoluteOutput = output.toAbsolutePath();
        Path parent = absoluteOutput.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Output path must have a parent directory");
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, absoluteOutput.getFileName().toString(), ".tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), pages);
            replaceAtomically(temporary, absoluteOutput);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void replaceAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
