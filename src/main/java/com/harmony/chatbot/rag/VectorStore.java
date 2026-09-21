package com.harmony.chatbot.rag;

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
    private record ScoredPage(Page page, double score) {}

    private final List<NormalizedPage> normalizedPages;
    private final int expectedDimensions;

    /**
     * Minimum cosine similarity a page must score to be included in results.
     * Pages below this threshold are considered irrelevant — including them
     * in the prompt context would confuse the model with unrelated content.
     * Tune this value based on your data (0.25–0.40 is a typical range).
     */
    public static final double MIN_SIMILARITY_THRESHOLD = 0.25;

    public VectorStore(Page[] pagesArray, int expectedDimensions) {
        if (expectedDimensions <= 0) {
            throw new IllegalArgumentException("Expected embedding dimensions must be positive");
        }
        this.expectedDimensions = expectedDimensions;
        List<Page> pages = pagesArray != null ? Arrays.asList(pagesArray) : Collections.emptyList();
        this.normalizedPages = preNormalize(pages);
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
            String pageId = page.getId() != null ? page.getId() : "<unknown>";
            if (emb == null || emb.length != expectedDimensions) {
                int actual = emb == null ? 0 : emb.length;
                throw new IllegalArgumentException("Page " + pageId + " has " + actual
                        + " embedding dimensions; expected " + expectedDimensions);
            }
            double norm = computeNorm(emb);
            if (norm < 1e-10) continue; // skip zero vectors
            result.add(new NormalizedPage(page, norm));
        }
        System.out.println("VectorStore pre-normalized " + result.size() + " pages.");
        return result;
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
        if (queryEmbedding.length != expectedDimensions) {
            throw new IllegalArgumentException("Query has " + queryEmbedding.length
                    + " embedding dimensions; expected " + expectedDimensions);
        }

        double queryNorm = computeNorm(queryEmbedding);
        if (queryNorm < 1e-10) return Collections.emptyList();

        return normalizedPages.stream()
                .map(np -> new ScoredPage(np.page(),
                        dotProduct(queryEmbedding, np.page().getEmbedding())
                                / (queryNorm * np.norm() + 1e-10)))
                .filter(scored -> scored.score() >= MIN_SIMILARITY_THRESHOLD)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(n)
                .map(ScoredPage::page)
                .collect(Collectors.toList());
    }

    private double computeNorm(double[] v) {
        double sum = 0.0;
        for (double x : v) sum += x * x;
        return Math.sqrt(sum);
    }

    private double dotProduct(double[] a, double[] b) {
        double dot = 0.0;
        for (int i = 0; i < expectedDimensions; i++) dot += a[i] * b[i];
        return dot;
    }

    public List<Page> getPages() {
        return normalizedPages.stream().map(NormalizedPage::page).collect(Collectors.toList());
    }
}
