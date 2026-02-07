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

    // Delete theme and avatar file
    public void deleteThemeForUser(UserEntity user) {
        getThemeEntityByUser(user).ifPresent(theme -> {

            // Delete avatar file if it exists
            if (theme.getAvatarFilename() != null && !theme.getAvatarFilename().isEmpty()) {
                File avatarFile = new File("uploads/avatar/" + theme.getAvatarFilename());
                if (avatarFile.exists()) {
                    avatarFile.delete();
                }
            }

            // Delete theme entity from DB
            themeRepository.delete(theme);
        });
    }

    // Existing methods (getOrCreateThemeForUser, updateThemeForUser, etc.) remain unchanged
}
