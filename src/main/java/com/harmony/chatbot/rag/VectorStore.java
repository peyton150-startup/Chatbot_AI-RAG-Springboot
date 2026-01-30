package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class VectorStore {

    private final List<Page> pages;

    // Constructor: initialize from Page array
    public VectorStore(Page[] pagesArray) {
        if (pagesArray != null) {
            this.pages = new ArrayList<>(Arrays.asList(pagesArray));
        } else {
            this.pages = new ArrayList<>();
        }
    }

    // Constructor: initialize from JSON file
    public VectorStore(String pagesFile) {
        this.pages = loadPagesFromFile(pagesFile);
    }

    private List<Page> loadPagesFromFile(String pagesFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Page> loadedPages;
        try {
            loadedPages = objectMapper.readValue(
                    new File(pagesFile),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Page.class)
            );
            System.out.println("Loaded " + loadedPages.size() + " pages from " + pagesFile);
        } catch (IOException e) {
            e.printStackTrace();
            loadedPages = new ArrayList<>();
        }
        return loadedPages;
    }

    public Page getMostRelevantPage(double[] queryEmbedding) {
        if (pages.isEmpty() || queryEmbedding == null || queryEmbedding.length == 0) return null;

        Page bestPage = null;
        double bestScore = -1;
        for (Page page : pages) {
            double[] pageEmb = page.getEmbedding();
            if (pageEmb == null || pageEmb.length == 0) continue;

            double score = cosineSimilarity(queryEmbedding, pageEmb);
            if (score > bestScore) {
                bestScore = score;
                bestPage = page;
            }
        }
        return bestPage;
    }

    /**
     * Returns the top N pages most similar to the query embedding.
     */
    public List<Page> getTopNPages(double[] queryEmbedding, int n) {
        if (pages.isEmpty() || queryEmbedding == null || queryEmbedding.length == 0 || n <= 0) {
            return new ArrayList<>();
        }

        return pages.stream()
                .filter(p -> p.getEmbedding() != null && p.getEmbedding().length > 0)
                .sorted((p1, p2) -> {
                    double score1 = cosineSimilarity(queryEmbedding, p1.getEmbedding());
                    double score2 = cosineSimilarity(queryEmbedding, p2.getEmbedding());
                    return Double.compare(score2, score1); // descending order
                })
                .limit(n)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) return 0.0;

        int len = Math.min(a.length, b.length);
        double dot = 0.0, normA = 0.0, normB = 0.0;

        for (int i = 0; i < len; i++) {
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
