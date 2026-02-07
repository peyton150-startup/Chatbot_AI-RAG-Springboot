package com.harmony.chatbot.chat;

import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import com.harmony.chatbot.rag.RAGService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RAGService ragService;
    private final UserService userService;
    private final ChatbotThemeService themeService;

    public ChatController(RAGService ragService,
                          UserService userService,
                          ChatbotThemeService themeService) {
        this.ragService = ragService;
        this.userService = userService;
        this.themeService = themeService;
    }

    /**
     * Returns the answer from the RAG service
     */
    @PostMapping("/chat")
    public String chat(@RequestBody String question) {
        return ragService.getAnswer(question);
    }

    /**
     * Returns the current logged-in user's theme as JSON
     */
    @GetMapping("/theme")
    public Map<String, String> getTheme() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch the UserEntity from the UserService
        UserEntity user = userService.getUserByUsernameOptional(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Fetch the theme for that user
        ChatbotThemeEntity theme = themeService.getThemeForUser(user.getId())
                .orElseThrow(() -> new IllegalStateException("Theme not found"));

        Map<String, String> themeMap = new HashMap<>();
        themeMap.put("headerColor", theme.getHeaderColor());
        themeMap.put("backgroundColor", theme.getBackgroundColor());
        themeMap.put("textColor", theme.getTextColor());
        themeMap.put("iconColor", theme.getIconColor());
        themeMap.put("avatarFilename", theme.getAvatarFilename() != null ? "/uploads/avatar/" + theme.getAvatarFilename() : null);

        // Optional chip colors
        themeMap.put("chipBackgroundColor", theme.getChipBackgroundColor() != null ? theme.getChipBackgroundColor() : "#f0f0f0");
        themeMap.put("chipHoverColor", theme.getChipHoverColor() != null ? theme.getChipHoverColor() : "#e0e0e0");
        themeMap.put("chipBorderColor", theme.getChipBorderColor() != null ? theme.getChipBorderColor() : "#ccc");

        return themeMap;
    }
}
