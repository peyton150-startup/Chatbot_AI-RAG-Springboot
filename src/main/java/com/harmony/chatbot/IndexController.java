package com.harmony.chatbot;

import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final ChatbotThemeService themeService;

    public IndexController(ChatbotThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        ChatbotThemeEntity theme = themeService.getThemeForCurrentUser();
        model.addAttribute("theme", theme);
        return "index";
    }
}
