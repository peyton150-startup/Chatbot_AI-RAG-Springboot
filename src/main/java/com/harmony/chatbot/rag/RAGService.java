package com.harmony.chatbot.rag;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RAGService {

    // Knowledge base
    private final Map<String, String> pages = new HashMap<>();

    // Keywords mapped to pages
    private final Map<String, String> keywordMap = new HashMap<>();

    public RAGService() {
        // Pages
        pages.put("office-location",
                "Harmony Aesthetics & Wellness is a trusted medical spa located in Kensington, Maryland and Falls Church, Virginia.");
        pages.put("hours",
                "The Kensington office hours are Monday through Friday from 9:00 am to 5:00 pm, Saturday from 9:00 am to 3:00 pm by appointment only, and closed on Sunday.");
        pages.put("staff",
                "The practice is run by Dr. Mario Ortega with board-certified NP Angelica and esthetician Alonnie.");
        pages.put("booking",
                "For appointments or questions, call or text (240) 280-0020 or book online at harmonyaestheticsandwellness.glossgenius.com.");

        // Keywords → page keys
        keywordMap.put("where is your office", "office-location");
        keywordMap.put("office location", "office-location");
        keywordMap.put("hours", "hours");
        keywordMap.put("opening hours", "hours");
        keywordMap.put("who works here", "staff");
        keywordMap.put("staff", "staff");
        keywordMap.put("book", "booking");
        keywordMap.pu
