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

    public ChatbotThemeService(ChatbotThemeRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;

        // Ensure upload directory exists
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * Retrieves the theme for the currently logged-in user.
     * If no theme exists, creates a default one.
     */
    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userService.getUserByUsernameOptional(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return repository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    newTheme.setHeaderColor("#ffffff");
                    newTheme.setBackgroundColor("#f5f5f5");
                    newTheme.setTextColor("#000000");
                    newTheme.setIconColor("#000000");
                    return repository.save(newTheme);
                });
    }

    /**
     * Updates the theme for the currently logged-in user, optionally updating avatar.
     */
    public ChatbotThemeEntity updateTheme(ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        ChatbotThemeEntity currentTheme = getThemeForCurrentUser();

        currentTheme.setHeaderColor(updatedTheme.getHeaderColor());
        currentTheme.setBackgroundColor(updatedTheme.getBackgroundColor());
        currentTheme.setTextColor(updatedTheme.getTextColor());
        currentTheme.setIconColor(updatedTheme.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            currentTheme.setAvatarFilename(filename);
        }

        return repository.save(currentTheme);
    }

    /**
     * Admin method: get theme by user ID.
     */
    public Optional<ChatbotThemeEntity> getThemeForUser(Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Admin method: update theme for a specific user.
     */
    public ChatbotThemeEntity updateThemeForUser(Long userId, ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatbotThemeEntity theme = repository.findByUserId(userId)
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    return repository.save(newTheme);
                });

        theme.setHeaderColor(updatedTheme.getHeaderColor());
        theme.setBackgroundColor(updatedTheme.getBackgroundColor());
        theme.setTextColor(updatedTheme.getTextColor());
        theme.setIconColor(updatedTheme.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            theme.setAvatarFilename(filename);
        }

        return repository.save(theme);
    }
}
