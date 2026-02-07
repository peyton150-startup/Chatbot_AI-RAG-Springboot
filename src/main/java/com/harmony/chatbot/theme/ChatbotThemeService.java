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

    /** ✅ ALWAYS returns a theme (creates one if missing) */
    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userService.getUserByUsernameOptional(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return createDefaultThemeIfMissing(user);
    }

    /** ✅ Used by UserService on user creation */
    public ChatbotThemeEntity createDefaultThemeIfMissing(UserEntity user) {
        return repository.findByUserId(user.getId())
                .orElseGet(() -> repository.save(createDefaultTheme(user)));
    }

    public ChatbotThemeEntity updateThemeForUser(
            Long userId,
            ChatbotThemeEntity updatedTheme,
            MultipartFile avatarFile
    ) throws IOException {

        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatbotThemeEntity theme = createDefaultThemeIfMissing(user);

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

    /** 🔒 Single source of truth for defaults */
    private ChatbotThemeEntity createDefaultTheme(UserEntity user) {
        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setUser(user);
        theme.setHeaderColor("#0d6efd");
        theme.setBackgroundColor("#ffffff");
        theme.setTextColor("#000000");
        theme.setIconColor("#0d6efd");
        return theme;
    }
}
