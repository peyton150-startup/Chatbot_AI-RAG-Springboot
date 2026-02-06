package com.harmony.chatbot.theme;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "theme")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String headerColor = "#b46a8c";
    private String backgroundColor = "#ffffff";
    private String textColor = "#000000";
    private String iconColor = "#b46a8c";

    private String avatarFilename; // stores the filename of uploaded avatar

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHeaderColor() { return headerColor; }
    public void setHeaderColor(String headerColor) { this.headerColor = headerColor; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }

    public String getAvatarFilename() { return avatarFilename; }
    public void setAvatarFilename(String avatarFilename) { this.avatarFilename = avatarFilename; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
