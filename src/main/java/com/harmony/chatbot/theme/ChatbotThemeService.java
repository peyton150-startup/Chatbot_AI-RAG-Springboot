package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
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

    public ChatbotThemeService(ChatbotThemeRepository repository,
                               UserService userService) {
        this.repository = repository;
        this.userService = userService;

        // Ensure uploads folder exists
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * Get the theme for the currently logged-in user.
     * If the user has no theme, create a default one for them.
     */
    public Theme getThemeForCurrentUser() {
        UserEntity currentUser = userService.getCurrentUser();
        Optional<Theme> themeOpt = repository.findByUserId(currentUser.getId());

        if (themeOpt.isPresent()) {
            return themeOpt.get();
        } else {
            Theme defaultTheme = new Theme();
            defaultTheme.setUser(currentUser);
            return repository.save(defaultTheme);
        }
    }

    /**
     * Update the theme for the currently logged-in user.
     */
    public Theme updateThemeForCurrentUser(Theme newTheme, MultipartFile avatarFile) throws IOException {
        Theme current = getThemeForCurrentUser();

        current.setHeaderColor(newTheme.getHeaderColor());
        current.setBackgroundColor(newTheme.getBackgroundColor());
        current.setTextColor(newTheme.getTextColor());
        current.setIconColor(newTheme.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            current.setAvatarFilename(filename);
        }

        return repository.save(current);
    }
}
