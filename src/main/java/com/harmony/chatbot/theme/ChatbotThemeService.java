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
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        System.out.println("ChatbotThemeService initialized with uploadDir: " + uploadDir);
    }

    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Getting theme for current user: " + username);
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
                    System.out.println("Creating default theme for user: " + username);
                    return repository.save(newTheme);
                });
    }

    public ChatbotThemeEntity updateThemeForUser(Long userId, ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println("Updating theme for user ID: " + userId + ", username: " + user.getUsername());

        ChatbotThemeEntity theme = repository.findByUserId(userId)
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    System.out.println("Creating new theme for user: " + user.getUsername());
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
            System.out.println("Avatar uploaded: " + filename);
        }

        return repository.save(theme);
    }

    public Optional<ChatbotThemeEntity> getThemeForUser(Long userId) {
        System.out.println("Fetching theme for userId: " + userId);
        return repository.findByUserId(userId);
    }
}
