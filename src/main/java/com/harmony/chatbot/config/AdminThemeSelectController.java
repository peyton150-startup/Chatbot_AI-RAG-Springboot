package com.harmony.chatbot.admin;

import com.harmony.chatbot.config.AppSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/active-theme")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminThemeSelectController {

    private final AppSettingsService appSettingsService;

    public AdminThemeSelectController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    /**
     * GET /admin/active-theme
     * Returns the currently active theme user ID so the page can highlight the
     * right row on load.
     */
    @GetMapping
    public Map<String, Object> getActiveTheme() {
        Long activeId = appSettingsService.getActiveThemeUserId();
        return Map.of("activeThemeUserId", activeId != null ? activeId : -1);
    }

    /**
     * POST /admin/active-theme { "userId": 3 }
     * Sets which user's theme is shown to anonymous widget visitors.
     */
    @PostMapping
    public ResponseEntity<String> setActiveTheme(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null)
            return ResponseEntity.badRequest().body("userId required");
        appSettingsService.setActiveThemeUserId(userId);
        return ResponseEntity.ok("ok");
    }
}