package com.harmony.chatbot.config;

import org.springframework.stereotype.Service;

@Service
public class AppSettingsService {

    private final AppSettingsRepository repository;

    public AppSettingsService(AppSettingsRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the active theme user ID, or null if none has been set
     * (in which case callers fall back to the admin user's theme).
     */
    public Long getActiveThemeUserId() {
        return repository.findById(1L)
                .map(AppSettingsEntity::getActiveThemeUserId)
                .orElse(null);
    }

    /**
     * Sets the active theme user ID and persists it.
     */
    public void setActiveThemeUserId(Long userId) {
        AppSettingsEntity settings = repository.findById(1L)
                .orElseGet(() -> {
                    AppSettingsEntity s = new AppSettingsEntity();
                    return s;
                });
        settings.setActiveThemeUserId(userId);
        repository.save(settings);
    }
}