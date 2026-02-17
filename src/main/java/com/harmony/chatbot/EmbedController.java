package com.harmony.chatbot;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmbedController {

    /**
     * Serves the embeddable chatbot script.
     * Any website can add: <script src="https://your-app.onrender.com/chatbot-embed.js"></script>
     */
    @GetMapping(value = "/chatbot-embed.js", produces = "application/javascript")
    public ResponseEntity<Resource> embedScript() {
        Resource resource = new ClassPathResource("static/chatbot-embed.js");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(resource);
    }
}
