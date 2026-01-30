package com.harmony.chatbot.rag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VectorStore {

    private final List<Page> pages;

    // New constructor: accept pages array directly
    public VectorStore(Page[] pagesArray) {
        if (pagesArray != null) {
            this.pages = new ArrayList<>(Arrays.asList(pagesArray));
        } else {
            this.pages = new ArrayList<>();
        }
    }

    public Page getMostRelevantPage(double[] queryEmbedding) {
        if (pages.isEmpty() || queryEmbedding == null || queryEmbedding.length == 0) return null;

        Page bestPage = null;
        double bestScore = -1;
        for (Page page : pages) {
            if (page.getEmbedding() == null) continue; // Skip pages without embedding
            double score = cosineSimilarity(queryEmbedding, page.getEmbedding());
            if (score > bestScore) {
                bestScore = score;
                bestPage = page;
            }
        }
        return bestPage;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }

    public List<Page> getPages() {
        return pages;
    }
}
