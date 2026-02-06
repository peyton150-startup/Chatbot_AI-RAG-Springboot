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
     * If no theme exists, creates a default one and saves it.
     *
     * @return ChatbotThemeEntity for the current user
     */
    public ChatbotThemeEntity getThemeForCurrentUser() {
        // Get currently authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        // Look for existing theme
        return repository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Create default theme if none exists
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    newTheme.setHeaderColor("#ffffff");    // default header color
                    newTheme.setBackgroundColor("#f5f5f5"); // default background
                    newTheme.setTextColor("#000000");       // default text
                    newTheme.setIconColor("#000000");       // default icon
                    return repository.save(newTheme);
                });
    }

    /**
     * Updates the theme for the currently logged-in user, optionally updating the avatar.
     *
     * @param updatedTheme The new theme values to save
     * @param avatarFile   Optional avatar file to upload
     * @return Updated ChatbotThemeEntity
     * @throws IOException if avatar upload fails
     */
    public ChatbotThemeEntity updateTheme(ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        ChatbotThemeEntity currentTheme = getThemeForCurrentUser();

        // Update theme colors
        currentTheme.setHeaderColor(updatedTheme.getHeaderColor());
        currentTheme.setBackgroundColor(updatedTheme.getBackgroundColor());
        currentTheme.setTextColor(updatedTheme.getTextColor());
        currentTheme.setIconColor(updatedTheme.getIconColor());

        // Handle avatar file upload if present
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            currentTheme.setAvatarFilename(filename);
        }

        // Save updated theme
        return repository.save(currentTheme);
    }
}
