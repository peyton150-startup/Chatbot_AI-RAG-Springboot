package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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
        if (!dir.exists()) dir.mkdirs();
    }

    public Theme getThemeForCurrentUser() {
        UserEntity currentUser = userService.getLoggedInUser();
        return repository.findByUser(currentUser).orElseGet(() -> {
            Theme theme = new Theme();
            theme.setUser(currentUser);
            return repository.save(theme);
        });
    }

    public Theme updateThemeForCurrentUser(Theme themeData, MultipartFile avatarFile) throws IOException {
        Theme current = getThemeForCurrentUser();

        current.setHeaderColor(themeData.getHeaderColor());
        current.setBackgroundColor(themeData.getBackgroundColor());
        current.setTextColor(themeData.getTextColor());
        current.setIconColor(themeData.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            current.setAvatarFilename(filename);
        }

        return repository.save(current);
    }
}
