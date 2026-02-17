package com.harmony.chatbot.config;

import jakarta.persistence.*;

/**
 * Stores a single row of global application settings.
 * We always read/write row with id=1 (singleton pattern).
 */
@Entity
@Table(name = "app_settings")
public class AppSettingsEntity {

    @Id
    private Long id = 1L;

    /**
     * The user ID whose theme is shown to anonymous visitors on the widget.
     * If null, falls back to the first ADMIN user's theme (original behaviour).
     */
    @Column(name = "active_theme_user_id")
    private Long activeThemeUserId;

    public Long getId() {
        return id;
    }

    public Long getActiveThemeUserId() {
        return activeThemeUserId;
    }

    public void setActiveThemeUserId(Long activeThemeUserId) {
        this.activeThemeUserId = activeThemeUserId;
    }
}