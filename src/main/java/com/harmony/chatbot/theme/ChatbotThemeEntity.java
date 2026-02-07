package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "chatbot_theme")
public class ChatbotThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserEntity user;

    @Column(nullable = false)
    private String headerColor = "#0d6efd";

    @Column(nullable = false)
    private String backgroundColor = "#ffffff";

    @Column(nullable = false)
    private String textColor = "#000000";

    @Column(nullable = false)
    private String iconColor = "#0d6efd";

    @Column
    private String avatarFilename;

    @Column
    private String chipBackgroundColor = "#e0e0e0";

    @Column
    private String chipHoverColor = "#c0c0c0";

    @Column
    private String chipBorderColor = "#b0b0b0";

    // ===== Getters / Setters =====
    public Long getId() { return id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

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

    public String getChipBackgroundColor() { return chipBackgroundColor; }
    public void setChipBackgroundColor(String chipBackgroundColor) { this.chipBackgroundColor = chipBackgroundColor; }

    public String getChipHoverColor() { return chipHoverColor; }
    public void setChipHoverColor(String chipHoverColor) { this.chipHoverColor = chipHoverColor; }

    public String getChipBorderColor() { return chipBorderColor; }
    public void setChipBorderColor(String chipBorderColor) { this.chipBorderColor = chipBorderColor; }
}
