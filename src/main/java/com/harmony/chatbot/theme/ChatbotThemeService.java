package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ChatbotThemeService(ChatbotThemeRepository themeRepository, UserRepository userRepository) {
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
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
            // Save file logic remains external; ensure file is saved to /uploads/avatar/
        }

        return themeRepository.save(theme);
    }

    public void deleteThemeForUser(UserEntity user) {
        getThemeEntityByUser(user).ifPresent(theme -> {

            // Delete avatar file
            if (theme.getAvatarFilename() != null && !theme.getAvatarFilename().isEmpty()) {
                File avatarFile = new File("uploads/avatar/" + theme.getAvatarFilename());
                if (avatarFile.exists()) {
                    avatarFile.delete();
                }
            }

            // Delete theme entity
            themeRepository.delete(theme);
        });
    }
}
