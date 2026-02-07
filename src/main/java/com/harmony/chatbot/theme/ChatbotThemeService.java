package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository themeRepository;

    public ChatbotThemeService(ChatbotThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ChatbotThemeEntity getOrCreateThemeForUser(UserEntity user) {
        return themeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity defaultTheme = new ChatbotThemeEntity();
                    defaultTheme.setUser(user);
                    defaultTheme.setHeaderColor("#0d6efd");
                    defaultTheme.setBackgroundColor("#ffffff");
                    defaultTheme.setTextColor("#000000");
                    defaultTheme.setIconColor("#0d6efd");
                    defaultTheme.setChipBackgroundColor("#f0f0f0");
                    defaultTheme.setChipHoverColor("#e0e0e0");
                    defaultTheme.setChipBorderColor("#ccc");
                    themeRepository.save(defaultTheme);
                    return defaultTheme;
                });
    }

    /** Default theme for anonymous visitors */
    public ChatbotThemeEntity getDefaultTheme() {
        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setHeaderColor("#0d6efd");
        theme.setBackgroundColor("#ffffff");
        theme.setTextColor("#000000");
        theme.setIconColor("#0d6efd");
        theme.setChipBackgroundColor("#f0f0f0");
        theme.setChipHoverColor("#e0e0e0");
        theme.setChipBorderColor("#ccc");
        theme.setAvatarFilename(null); // No avatar for anonymous
        return theme;
    }
}
