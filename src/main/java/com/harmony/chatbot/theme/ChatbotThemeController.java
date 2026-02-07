package com.harmony.chatbot.theme;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatbotThemeController {

    private final ChatbotThemeService themeService;

    public ChatbotThemeController(ChatbotThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/api/theme")
    public ChatbotThemeEntity getCurrentUserTheme() {
        // No arguments needed; service uses SecurityContextHolder internally
        return themeService.getThemeForCurrentUser();
    }
}
