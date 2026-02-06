package com.harmony.chatbot.theme;

import jakarta.persistence.*;

@Entity
@Table(name = "chatbot_theme")
public class ChatbotThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public Long getId() { return id; }

    public String getHeaderColor() { return headerColor; }
    public void setHeaderColor(String headerColor) { this.headerColor = headerColor; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }

    public String getAvatarFilename() { return avatarFilename; }
    public void setAvatarFilename(String avatarFilename) {
        this.avatarFilename = avatarFilename;
    }
}
