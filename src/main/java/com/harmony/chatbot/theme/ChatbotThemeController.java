package com.harmony.chatbot.theme;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/theme")
public class ChatbotThemeController {

    private final ChatbotThemeService themeService;

    public ChatbotThemeController(ChatbotThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ChatbotThemeEntity getTheme() {
        return themeService.getThemeForCurrentUser();
    }

    @PostMapping
    public ChatbotThemeEntity updateTheme(@RequestParam(required = false) MultipartFile avatar,
                                          @RequestParam String headerColor,
                                          @RequestParam String backgroundColor,
                                          @RequestParam String textColor,
                                          @RequestParam String iconColor,
                                          @RequestParam(required = false) String chipBackgroundColor,
                                          @RequestParam(required = false) String chipHoverColor,
                                          @RequestParam(required = false) String chipBorderColor) throws Exception {

        ChatbotThemeEntity updatedTheme = new ChatbotThemeEntity();
        updatedTheme.setHeaderColor(headerColor);
        updatedTheme.setBackgroundColor(backgroundColor);
        updatedTheme.setTextColor(textColor);
        updatedTheme.setIconColor(iconColor);
        updatedTheme.setChipBackgroundColor(chipBackgroundColor);
        updatedTheme.setChipHoverColor(chipHoverColor);
        updatedTheme.setChipBorderColor(chipBorderColor);

        Long userId = themeService.getThemeForCurrentUser().getUser().getId();
        return themeService.updateThemeForUser(userId, updatedTheme, avatar);
    }
}
