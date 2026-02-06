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
    }

    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userService.getUserByUsernameOptional(username).orElseThrow();

        return repository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    return repository.save(newTheme);
                });
    }

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

    // New method: update theme by userId (used in AdminController)
    public ChatbotThemeEntity updateThemeForUser(Long userId, ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        UserEntity user = userService.getUserById(userId).orElseThrow();
        ChatbotThemeEntity theme = repository.findByUserId(userId)
                .orElseGet(() -> {
                    ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
                    newTheme.setUser(user);
                    return newTheme;
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

    public Optional<ChatbotThemeEntity> getThemeForUser(Long userId) {
        return repository.findByUserId(userId);
    }
}
