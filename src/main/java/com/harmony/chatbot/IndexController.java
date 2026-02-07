package com.harmony.chatbot;

import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final ChatbotThemeService themeService;
    private final UserService userService;

    public IndexController(ChatbotThemeService themeService,
                           UserService userService) {
        this.themeService = themeService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Default theme if not logged in
        ChatbotThemeEntity theme = new ChatbotThemeEntity();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userService.getUserByUsernameOptional(auth.getName())
                    .ifPresent(user -> themeService.getOrCreateThemeForUser(user.getId()));
        }

        model.addAttribute("theme", theme);

        return "index";
    }
}
