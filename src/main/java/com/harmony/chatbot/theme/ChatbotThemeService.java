package com.harmony.chatbot.theme;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository repository;
    private final String uploadDir = "uploads/avatar/";

    public ChatbotThemeService(ChatbotThemeRepository repository) {
        this.repository = repository;

        // Create uploads folder if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    public Theme getTheme() {
        List<Theme> all = repository.findAll();
        if (all.isEmpty()) {
            Theme t = new Theme();
            repository.save(t);
            return t;
        }
        return all.get(0); // single global theme
    }

    public Theme updateTheme(Theme theme, MultipartFile avatarFile) throws IOException {
        Theme current = getTheme();

        current.setHeaderColor(theme.getHeaderColor());
        current.setBackgroundColor(theme.getBackgroundColor());
        current.setTextColor(theme.getTextColor());
        current.setIconColor(theme.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            current.setAvatarFilename(filename);
        }

        return repository.save(current);
    }
}
