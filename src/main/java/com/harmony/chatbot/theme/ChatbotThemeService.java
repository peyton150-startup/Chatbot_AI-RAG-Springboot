package com.harmony.chatbot.theme;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository repository;
    private static final String AVATAR_DIR = "uploads/avatar/";

    public ChatbotThemeService(ChatbotThemeRepository repository) {
        this.repository = repository;
    }

    public ChatbotThemeEntity getTheme() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> repository.save(new ChatbotThemeEntity()));
    }

    public void updateTheme(ChatbotThemeEntity updated,
                            MultipartFile avatarFile) throws Exception {

        ChatbotThemeEntity theme = getTheme();

        theme.setHeaderColor(updated.getHeaderColor());
        theme.setBackgroundColor(updated.getBackgroundColor());
        theme.setTextColor(updated.getTextColor());
        theme.setIconColor(updated.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = UUID.randomUUID() + "-" + avatarFile.getOriginalFilename();

            File dir = new File(AVATAR_DIR);
            if (!dir.exists()) dir.mkdirs();

            avatarFile.transferTo(new File(dir, filename));
            theme.setAvatarFilename(filename);
        }

        repository.save(theme);
    }
}
