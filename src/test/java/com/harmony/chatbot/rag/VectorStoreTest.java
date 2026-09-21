package com.harmony.chatbot.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VectorStoreTest {

    @Test
    void returnsTheNearestPageUsingTheEntireExpectedVector() {
        VectorStore store = new VectorStore(new Page[]{
                page("boarding", new double[]{1.0, 0.0}),
                page("grooming", new double[]{0.0, 1.0})
        }, 2);

        List<Page> matches = store.getTopNPages(new double[]{0.9, 0.1}, 1);

        assertEquals(1, matches.size());
        assertEquals("boarding", matches.get(0).getId());
    }

    @Test
    void rejectsStoredEmbeddingsWithTheWrongDimension() {
        Page wrong = page("wrong", new double[]{1.0});

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new VectorStore(new Page[]{wrong}, 2));

        assertEquals("Page wrong has 1 embedding dimensions; expected 2", error.getMessage());
    }

    @Test
    void rejectsQueryEmbeddingsWithTheWrongDimension() {
        VectorStore store = new VectorStore(
                new Page[]{page("boarding", new double[]{1.0, 0.0})}, 2);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> store.getTopNPages(new double[]{1.0}, 1));

        assertEquals("Query has 1 embedding dimensions; expected 2", error.getMessage());
    }

    private Page page(String id, double[] embedding) {
        Page page = new Page();
        page.setId(id);
        page.setSource("source-" + id);
        page.setText("Information about " + id);
        page.setEmbedding(embedding);
        return page;
    }
}
