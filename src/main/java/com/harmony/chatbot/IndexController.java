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

    public IndexController(ChatbotThemeService themeService, UserService userService) {
        this.themeService = themeService;
        this.userService = userService;
    }

    // Render main page
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("theme", getThemeForCurrentUser());
        return "index";
    }

    // Render login page
    @GetMapping("/login")
    public String login(Model model) {
        // Always use default theme for login
        model.addAttribute("theme", themeService.getDefaultTheme());
        return "login";
    }

    // Helper: return theme for current user or default if anonymous
    private ChatbotThemeEntity getThemeForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            UserEntity currentUser = userService.getUserByUsernameOptional(auth.getName()).orElse(null);
            if (currentUser != null) {
                return themeService.getOrCreateThemeForUser(currentUser);
            }
        }
        return themeService.getDefaultTheme();
    }
}
