package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatbotThemeController {

    private final ChatbotThemeService themeService;
    private final UserService userService;

    public ChatbotThemeController(ChatbotThemeService themeService,
                                  UserService userService) {
        this.themeService = themeService;
        this.userService = userService;
    }

    @GetMapping("/api/theme")
    public ChatbotThemeEntity getCurrentUserTheme() {
        // Pass the userService to themeService to fetch current user's theme
        return themeService.getThemeForCurrentUser(userService);
    }
}
