package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository themeRepository;

    // Lazy injection to break circular dependency
    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    public ChatbotThemeService(ChatbotThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Optional<ChatbotThemeEntity> getThemeEntityByUser(UserEntity user) {
        return themeRepository.findByUserId(user.getId());
    }

    public ChatbotThemeEntity getOrCreateThemeForUser(UserEntity user) {
        return themeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity theme = new ChatbotThemeEntity();
                    theme.setUser(user);
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

    public ChatbotThemeEntity updateThemeForUser(UserEntity user, ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) {
        ChatbotThemeEntity theme = themeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    return newTheme;
                });

        theme.setHeaderColor(updatedTheme.getHeaderColor());
        theme.setBackgroundColor(updatedTheme.getBackgroundColor());
        theme.setTextColor(updatedTheme.getTextColor());
        theme.setIconColor(updatedTheme.getIconColor());
        theme.setChipBackgroundColor(updatedTheme.getChipBackgroundColor());
        theme.setChipHoverColor(updatedTheme.getChipHoverColor());
        theme.setChipBorderColor(updatedTheme.getChipBorderColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = avatarFile.getOriginalFilename();
            theme.setAvatarFilename(filename);
            // Save file externally
        }

        return themeRepository.save(theme);
    }

    public void deleteThemeForUser(UserEntity user) {
        getThemeEntityByUser(user).ifPresent(theme -> {
            if (theme.getAvatarFilename() != null && !theme.getAvatarFilename().isEmpty()) {
                File avatarFile = new File("uploads/avatar/" + theme.getAvatarFilename());
                if (avatarFile.exists()) avatarFile.delete();
            }
            themeRepository.delete(theme);
        });
    }

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

    public ChatbotThemeEntity getThemeForCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            UserEntity user = userService.getUserByUsernameOptional(userDetails.getUsername()).orElse(null);
            if (user != null) return getOrCreateThemeForUser(user);
        }
        return getDefaultTheme();
    }
}
