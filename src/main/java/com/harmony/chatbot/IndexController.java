package com.harmony.chatbot;

import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.user.UserService;
import com.harmony.chatbot.user.UserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final ChatbotThemeService themeService;
    private final UserService userService;

    public IndexController(ChatbotThemeService themeService, UserService userService) {
        this.themeService = themeService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Fetch currently authenticated user entity
        UserEntity currentUser = userService.getUserByUsernameOptional(
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
        ).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        // Get theme for this user
        ChatbotThemeEntity theme = themeService.getOrCreateThemeForUser(currentUser);
        model.addAttribute("theme", theme);

        return "index";
    }
}
