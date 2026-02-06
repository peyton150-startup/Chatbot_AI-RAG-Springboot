
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@@ -16,51 +17,51 @@ public class ChatbotThemeService {
    private final UserService userService;
    private final String uploadDir = "uploads/avatar/";

    public ChatbotThemeService(ChatbotThemeRepository repository,
                               UserService userService) {
    public ChatbotThemeService(ChatbotThemeRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;

        // Ensure uploads folder exists
        // Ensure upload directory exists
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * Get the theme for the currently logged-in user.
     * If the user has no theme, create a default one for them.
     * Retrieves the theme for the currently logged-in user.
     * If no theme exists, creates a default one.
     */
    public Theme getThemeForCurrentUser() {
        UserEntity currentUser = userService.getCurrentUser();
        Optional<Theme> themeOpt = repository.findByUserId(currentUser.getId());
    public ChatbotThemeEntity getThemeForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userService.getUserByUsername(username).orElseThrow();

        Optional<ChatbotThemeEntity> themeOpt = repository.findByUserId(user.getId());
        if (themeOpt.isPresent()) {
            return themeOpt.get();
        } else {
            Theme defaultTheme = new Theme();
            defaultTheme.setUser(currentUser);
            return repository.save(defaultTheme);
            ChatbotThemeEntity newTheme = new ChatbotThemeEntity();
            newTheme.setUser(user);
            return repository.save(newTheme);
        }
    }

    /**
     * Update the theme for the currently logged-in user.
     * Updates the theme for the current user, including optional avatar upload
     */
    public Theme updateThemeForCurrentUser(Theme newTheme, MultipartFile avatarFile) throws IOException {
        Theme current = getThemeForCurrentUser();
    public ChatbotThemeEntity updateTheme(ChatbotThemeEntity updatedTheme, MultipartFile avatarFile) throws IOException {
        ChatbotThemeEntity currentTheme = getThemeForCurrentUser();

        current.setHeaderColor(newTheme.getHeaderColor());
        current.setBackgroundColor(newTheme.getBackgroundColor());
        current.setTextColor(newTheme.getTextColor());
        current.setIconColor(newTheme.getIconColor());
        currentTheme.setHeaderColor(updatedTheme.getHeaderColor());
        currentTheme.setBackgroundColor(updatedTheme.getBackgroundColor());
        currentTheme.setTextColor(updatedTheme.getTextColor());
        currentTheme.setIconColor(updatedTheme.getIconColor());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            File file = new File(uploadDir + filename);
            avatarFile.transferTo(file);
            current.setAvatarFilename(filename);
            currentTheme.setAvatarFilename(filename);
        }

        return repository.save(current);
        return repository.save(currentTheme);
    }
}
