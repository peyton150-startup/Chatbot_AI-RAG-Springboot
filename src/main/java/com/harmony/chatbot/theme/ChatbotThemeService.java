package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ChatbotThemeService(ChatbotThemeRepository themeRepository, UserRepository userRepository) {
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    // Get or create a theme for a specific user
    public ChatbotThemeEntity getOrCreateThemeForUser(UserEntity user) {
        Optional<ChatbotThemeEntity> optionalTheme = themeRepository.findByUserId(user.getId());
        return optionalTheme.orElseGet(() -> {
            ChatbotThemeEntity theme = new ChatbotThemeEntity();
            theme.setUser(user);

            // Set default colors
            theme.setHeaderColor("#0d6efd");
            theme.setBackgroundColor("#ffffff");
            theme.setTextColor("#000000");
            theme.setIconColor("#0d6efd");

            theme.setChipBackgroundColor("#f0f0f0");
            theme.setChipHoverColor("#e0e0e0");
            theme.setChipBorderColor("#ccc");

            return themeRepository.save(theme);
        });
    }

    // Get theme for currently authenticated user
    public ChatbotThemeEntity getThemeForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            UserEntity user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                return getOrCreateThemeForUser(user);
            }
        }

        return getDefaultTheme();
    }

    // Update a user's theme
    public ChatbotThemeEntity updateThemeForUser(UserEntity user, ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) {
        ChatbotThemeEntity theme = themeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    return newTheme;
                });

        // Update color properties
        theme.setHeaderColor(updatedTheme.getHeaderColor());
        theme.setBackgroundColor(updatedTheme.getBackgroundColor());
        theme.setTextColor(updatedTheme.getTextColor());
        theme.setIconColor(updatedTheme.getIconColor());
        theme.setChipBackgroundColor(updatedTheme.getChipBackgroundColor());
        theme.setChipHoverColor(updatedTheme.getChipHoverColor());
        theme.setChipBorderColor(updatedTheme.getChipBorderColor());

        // Handle avatar file upload if provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = avatarFile.getOriginalFilename();
            // TODO: save file to /uploads/avatar/ and set filename
            theme.setAvatarFilename(filename);
        }

        return themeRepository.save(theme);
    }

    // Default theme if no user is logged in
    public ChatbotThemeEntity getDefaultTheme() {
        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setHeaderColor("#0d6efd");
        theme.setBackgroundColor("#ffffff");
        theme.setTextColor("#000000");
        theme.setIconColor("#0d6efd");

        theme.setChipBackgroundColor("#f0f0f0");
        theme.setChipHoverColor("#e0e0e0");
        theme.setChipBorderColor("#ccc");

        return theme;
    }

    // --- NEW METHOD: delete a theme for a given user ---
    public void deleteThemeForUser(UserEntity user) {
        themeRepository.findByUserId(user.getId())
                .ifPresent(themeRepository::delete);
    }
}
