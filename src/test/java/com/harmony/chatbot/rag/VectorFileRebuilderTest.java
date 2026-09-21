package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.chatbot.ai.AiClient;
import com.harmony.chatbot.ai.AiMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VectorFileRebuilderTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path tempDirectory;

    @Test
    void replacesEmbeddingsWhilePreservingPageDataAndOrder() throws Exception {
        Path input = tempDirectory.resolve("input.json");
        Path output = tempDirectory.resolve("output.json");
        mapper.writeValue(input.toFile(), new Page[]{
                page("one", "source one", "first passage", new double[]{9.0}),
                page("two", "source two", "second passage", new double[]{8.0})
        });
        RecordingAiClient aiClient = new RecordingAiClient(List.of(
                new double[]{0.1, 0.2},
                new double[]{0.3, 0.4}
        ));

        new VectorFileRebuilder(aiClient, mapper, 16).rebuild(input, output);

        Page[] rebuilt = mapper.readValue(output.toFile(), Page[].class);
        assertEquals(2, rebuilt.length);
        assertEquals("one", rebuilt[0].getId());
        assertEquals("source one", rebuilt[0].getSource());
        assertEquals("first passage", rebuilt[0].getText());
        assertArrayEquals(new double[]{0.1, 0.2}, rebuilt[0].getEmbedding());
        assertEquals("two", rebuilt[1].getId());
        assertArrayEquals(new double[]{0.3, 0.4}, rebuilt[1].getEmbedding());
        assertEquals(List.of("first passage", "second passage"), aiClient.embeddedPassages);
    }

    @Test
    void requestsPassageEmbeddingsInBoundedBatches() throws Exception {
        Path input = tempDirectory.resolve("input.json");
        Path output = tempDirectory.resolve("output.json");
        mapper.writeValue(input.toFile(), new Page[]{
                page("one", "s1", "first", null),
                page("two", "s2", "second", null),
                page("three", "s3", "third", null)
        });
        RecordingAiClient aiClient = new RecordingAiClient(List.of(
                new double[]{0.1}, new double[]{0.2}, new double[]{0.3}
        ));

        new VectorFileRebuilder(aiClient, mapper, 2).rebuild(input, output);

        assertEquals(List.of(2, 1), aiClient.batchSizes);
    }

    @Test
    void rejectsPagesWithBlankTextBeforeCallingTheProvider() throws Exception {
        Path input = tempDirectory.resolve("input.json");
        Path output = tempDirectory.resolve("output.json");
        mapper.writeValue(input.toFile(), new Page[]{page("one", "source", " ", null)});
        RecordingAiClient aiClient = new RecordingAiClient(List.of(new double[]{0.1}));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new VectorFileRebuilder(aiClient, mapper, 16).rebuild(input, output));

        assertEquals("Page one has blank text", error.getMessage());
        assertEquals(List.of(), aiClient.batchSizes);
    }

    private Page page(String id, String source, String text, double[] embedding) {
        Page page = new Page();
        page.setId(id);
        page.setSource(source);
        page.setText(text);
        page.setEmbedding(embedding);
        return page;
    }

    private static final class RecordingAiClient implements AiClient {
        private final List<double[]> responses;
        private final List<String> embeddedPassages = new ArrayList<>();
        private final List<Integer> batchSizes = new ArrayList<>();
        private int responseIndex;

        private RecordingAiClient(List<double[]> responses) {
            this.responses = responses;
        }

        @Override
        public double[] embedQuery(String text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<double[]> embedPassages(List<String> texts) {
            batchSizes.add(texts.size());
            embeddedPassages.addAll(texts);
            List<double[]> batch = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                batch.add(responses.get(responseIndex++));
            }
            return batch;
        }

        @Override
        public String chat(List<AiMessage> messages) {
            throw new UnsupportedOperationException();
        }
    }
}
