package com.harmony.chatbot.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/admin/rag")
@PreAuthorize("hasAuthority('ADMIN')")
public class RagUploadController {

    private final RAGService ragService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagUploadController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * POST /admin/rag/upload
     * Accepts a new vectors.json and hot-reloads the RAGService vector store
     * without requiring a redeploy.
     *
     * We deserialize using the same ObjectMapper + Page[] pattern that RAGService
     * uses on startup so field mapping is guaranteed to be consistent.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVectors(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided.");
        }

        try {
            byte[] bytes = file.getBytes();

            // Deserialize exactly the same way RAGService does at startup
            Page[] pages = objectMapper.readValue(bytes, Page[].class);

            if (pages == null || pages.length == 0) {
                return ResponseEntity.badRequest().body(
                        "File parsed to 0 pages — check that the JSON is a non-empty array.");
            }

            // Spot-check first entry has a usable embedding and text
            if (pages[0].getEmbedding() == null || pages[0].getEmbedding().length == 0) {
                return ResponseEntity.badRequest().body(
                        "First page has no embedding. Check that your JSON uses the correct field name (e.g. 'embedding').");
            }
            if (pages[0].getText() == null || pages[0].getText().isBlank()) {
                return ResponseEntity.badRequest().body(
                        "First page has no text. Check that your JSON uses the correct field name (e.g. 'text').");
            }

            ragService.reloadVectorStore(pages);

            return ResponseEntity.ok(Map.of(
                    "pageCount", pages.length,
                    "message", "Vector store reloaded successfully."));

        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            return ResponseEntity.badRequest().body("Invalid JSON: " + e.getOriginalMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}