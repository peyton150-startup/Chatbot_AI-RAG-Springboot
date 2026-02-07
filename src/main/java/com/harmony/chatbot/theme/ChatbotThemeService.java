package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository repository;
    private final UserService userService;
    private final String uploadDir = "uploads/avatar/";

    public ChatbotThemeService(ChatbotThemeRepository repository,
                               UserService userService) {
        this.repository = repository;
        this.userService = userService;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Get or create a theme for the currently authenticated user
     */
    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userService.getUserByUsernameOptional(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return repository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultTheme(user));
    }

    /**
     * Get a theme by userId or create default
     */
    public ChatbotThemeEntity getOrCreateThemeForUser(Long userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    UserEntity user = userService.getUserById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    return createDefaultTheme(user);
                });
    }

    /**
     * Update theme values
     */
    public ChatbotThemeEntity updateThemeForUser(
            Long userId,
            ChatbotThemeEntity updatedTheme,
            MultipartFile avatarFile
    ) throws IOException {
        ChatbotThemeEntity theme = repository.findByUserId(userId)
                .orElseGet(() -> getOrCreateThemeForUser(userId));

        theme.setHeaderColor(updatedTheme.getHeaderColor());
        theme.setBackgroundColor(updatedTheme.getBackgroundColor());
        theme.setTextColor(updatedTheme.getTextColor());
        theme.setIconColor(updatedTheme.getIconColor());
        theme.setChipBackgroundColor(updatedTheme.getChipBackgroundColor());
        theme.setChipHoverColor(updatedTheme.getChipHoverColor());
        theme.setChipBorderColor(updatedTheme.getChipBorderColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            theme.setAvatarFilename(filename);
        }

        return repository.save(theme);
    }

    private ChatbotThemeEntity createDefaultTheme(UserEntity user) {
        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setUser(user);
        return repository.save(theme);
    }

    public Optional<ChatbotThemeEntity> getThemeForUser(Long userId) {
        return repository.findByUserId(userId);
    }
}
