package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VectorStore {

    /**
     * Wraps a Page with its pre-computed L2 norm so we skip recomputing it
     * on every similarity query. For large vector stores this is a meaningful
     * speed-up since norms only need to be computed once at load time.
     */
    private record NormalizedPage(Page page, double norm) {}

    private final List<NormalizedPage> normalizedPages;

    /**
     * Minimum cosine similarity a page must score to be included in results.
     * Pages below this threshold are considered irrelevant — including them
     * in the prompt context would confuse the model with unrelated content.
     * Tune this value based on your data (0.25–0.40 is a typical range).
     */
    public static final double MIN_SIMILARITY_THRESHOLD = 0.25;

    public VectorStore(Page[] pagesArray) {
        List<Page> pages = pagesArray != null ? Arrays.asList(pagesArray) : Collections.emptyList();
        this.normalizedPages = preNormalize(pages);
    }

    public VectorStore(String pagesFile) {
        this.normalizedPages = preNormalize(loadPagesFromFile(pagesFile));
    }

    /**
     * Pre-compute the L2 norm for every page embedding at load time.
     * Pages with null/empty embeddings are filtered out here once,
     * rather than being checked on every query.
     */
    private List<NormalizedPage> preNormalize(List<Page> pages) {
        List<NormalizedPage> result = new ArrayList<>();
        for (Page page : pages) {
            double[] emb = page.getEmbedding();
            if (emb == null || emb.length == 0) continue;
            double norm = computeNorm(emb);
            if (norm < 1e-10) continue; // skip zero vectors
            result.add(new NormalizedPage(page, norm));
        }
        System.out.println("VectorStore pre-normalized " + result.size() + " pages.");
        return result;
    }

    private List<Page> loadPagesFromFile(String pagesFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Page> loadedPages = objectMapper.readValue(
                    new File(pagesFile),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Page.class)
            );
            System.out.println("Loaded " + loadedPages.size() + " pages from " + pagesFile);
            return loadedPages != null ? loadedPages : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Returns the single most relevant page for the query embedding,
     * or null if no page meets the minimum similarity threshold.
     */
    public Page getMostRelevantPage(double[] queryEmbedding) {
        List<Page> top = getTopNPages(queryEmbedding, 1);
        return top.isEmpty() ? null : top.get(0);
    }

    /**
     * Returns the top N pages most similar to the query embedding.
     * Pages scoring below MIN_SIMILARITY_THRESHOLD are excluded entirely —
     * this prevents irrelevant context from being sent to the model.
     */
    public List<Page> getTopNPages(double[] queryEmbedding, int n) {
        if (normalizedPages.isEmpty() || queryEmbedding == null || queryEmbedding.length == 0 || n <= 0) {
            return Collections.emptyList();
        }

        double queryNorm = computeNorm(queryEmbedding);
        if (queryNorm < 1e-10) return Collections.emptyList();

        return normalizedPages.stream()
                .map(np -> {
                    double score = dotProduct(queryEmbedding, np.page().getEmbedding())
                                   / (queryNorm * np.norm() + 1e-10);
                    return new double[]{ score, normalizedPages.indexOf(np) };
                })
                .filter(pair -> pair[0] >= MIN_SIMILARITY_THRESHOLD)
                .sorted((a, b) -> Double.compare(b[0], a[0]))
                .limit(n)
                .map(pair -> normalizedPages.get((int) pair[1]).page())
                .collect(Collectors.toList());
    }

    private double computeNorm(double[] v) {
        double sum = 0.0;
        for (double x : v) sum += x * x;
        return Math.sqrt(sum);
    }

    private double dotProduct(double[] a, double[] b) {
        int len = Math.min(a.length, b.length);
        double dot = 0.0;
        for (int i = 0; i < len; i++) dot += a[i] * b[i];
        return dot;
    }

    public List<Page> getPages() {
        return normalizedPages.stream().map(NormalizedPage::page).collect(Collectors.toList());
    }
}