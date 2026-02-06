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
        UserEntity user = userService.getUserByUsername(username).orElseThrow();

        Optional<ChatbotThemeEntity> themeOpt = repository.findByUserId(user.getId());
        if (themeOpt.isPresent()) {
            return themeOpt.get();
        } else {
            ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
            newTheme.setUser(user);
            return repository.save(newTheme);
        }
    }

    /**
     * Updates the theme for the current user, including optional avatar upload
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
}
