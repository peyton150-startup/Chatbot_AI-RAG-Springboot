package com.harmony.chatbot.rag;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RAGService {

    // Your "knowledge base" — only pages from the site
    private final Map<String, String> pages = new HashMap<>();

    public RAGService() {
        // Add your actual page content here
        pages.put("office-location",
                "Harmony Aesthetics & Wellness is a trusted medical spa located in Kensington, Maryland and Falls Church, Virginia.");
        pages.put("hours",
                "The Kensington office hours are Monday through Friday from 9:00 am to 5:00 pm, Saturday from 9:00 am to 3:00 pm by appointment only, and closed on Sunday.");
        pages.put("staff",
                "The practice is run by Dr. Mario Ortega with board-certified NP Angelica and esthetician Alonnie.");
        pages.put("booking",
                "For appointments or questions, call or text (240) 280-0020 or book online at harmonyaestheticsandwellness.glossgenius.com.");
    }

    /**
     * Retrieve answer strictly from the pages.
     */
    public String getAnswer(String question) {
        String lower = question.toLowerCase();

        // naive keyword search, can improve later
        for (Map.Entry<String, String> entry : pages.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // If no match, MUST NOT hallucinate
        return "I don’t have that information in my knowledge base.";
    }
}
