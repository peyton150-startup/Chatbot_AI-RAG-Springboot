package com.harmony.chatbot.theme;

import com.harmony.chatbot.config.AppSettingsService;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/theme")
public class ChatbotThemeController {

    private final ChatbotThemeService themeService;
    private final UserService userService;
    private final AppSettingsService appSettingsService;

    public ChatbotThemeController(ChatbotThemeService themeService,
            UserService userService,
            AppSettingsService appSettingsService) {
        this.themeService = themeService;
        this.userService = userService;
        this.appSettingsService = appSettingsService;
    }

    /**
     * GET /api/theme
     * - If logged in: return that user's own theme.
     * - If anonymous: return whichever user's theme has been selected as active
     * via the admin dashboard. Falls back to the first admin user if none set.
     */
    @GetMapping
    public ChatbotThemeEntity getCurrentUserTheme() {
        UserEntity authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser != null) {
            return themeService.getOrCreateThemeForUser(authenticatedUser);
        }

        // Anonymous visitor — check if an active theme user has been selected
        Long activeUserId = appSettingsService.getActiveThemeUserId();
        if (activeUserId != null) {
            return userService.getUserById(activeUserId)
                    .map(themeService::getOrCreateThemeForUser)
                    .orElseGet(this::fallbackToAdminTheme);
        }

        return fallbackToAdminTheme();
    }

    private ChatbotThemeEntity fallbackToAdminTheme() {
        return userService.getAdminUser()
                .map(themeService::getOrCreateThemeForUser)
                .orElseGet(themeService::getDefaultTheme);
    }

    /**
     * GET /api/theme/{userId}
     * Admin-only: fetch a specific user's theme by their ID.
     * Used by the admin dashboard to load the correct colours/avatar/banner
     * into the theme form when the active theme user is switched.
     */
    @GetMapping("/{userId}")
    public ChatbotThemeEntity getThemeByUserId(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(themeService::getOrCreateThemeForUser)
                .orElseGet(themeService::getDefaultTheme);
    }

    /**
     * POST /api/theme/save
     * Save theme as JSON (no file upload). Used when no avatar is being changed.
     */
    @PostMapping("/save")
    public ChatbotThemeEntity saveTheme(@RequestBody ChatbotThemeEntity updatedTheme) {
        UserEntity user = getAuthenticatedUser();
        if (user != null) {
            return themeService.updateThemeForUser(user, updatedTheme, null);
        }
        return updatedTheme;
    }

    /**
     * DELETE /api/theme/delete
     * Reset the current user's theme.
     */
    @DeleteMapping("/delete")
    public String deleteTheme() {
        UserEntity user = getAuthenticatedUser();
        if (user != null) {
            themeService.deleteThemeForUser(user);
            return "Theme deleted";
        }
        return "No user logged in";
    }

    private UserEntity getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userService.getUserByUsernameOptional(userDetails.getUsername()).orElse(null);
        }
        return null;
    }
}