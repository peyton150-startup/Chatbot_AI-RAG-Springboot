package com.harmony.chatbot.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    /**
     * One Bucket4j bucket per IP address.
     * Each bucket allows MAX_REQUESTS tokens, refilled fully every WINDOW_MINUTES.
     * Bucket4j handles all the thread-safety and window management internally —
     * no manual window resets or AtomicInteger bookkeeping needed.
     */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_MINUTES = 1;

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS,
                Refill.greedy(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // Only rate-limit the chat endpoint
        if ("/api/chat".equals(httpReq.getRequestURI())) {
            String ip = getClientIp(httpReq);
            Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

            if (!bucket.tryConsume(1)) {
                httpResp.setStatus(429);
                httpResp.setContentType("application/json");
                httpResp.getWriter().write(
                        "{\"error\":\"Too many requests. Please wait a moment before asking again.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}