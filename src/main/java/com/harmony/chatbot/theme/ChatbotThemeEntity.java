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
    @JoinColumn(name = "user_id", unique = true,
                foreignKey = @ForeignKey(name = "fk_theme_user"),
                nullable = false)
    private UserEntity user;

    @Column(nullable = false) private String headerColor = "#0d6efd";
    @Column(nullable = false) private String backgroundColor = "#ffffff";
    @Column(nullable = false) private String textColor = "#000000";
    @Column(nullable = false) private String iconColor = "#0d6efd";
    @Column private String chipBackgroundColor = "#f0f0f0";
    @Column private String chipHoverColor = "#e0e0e0";
    @Column private String chipBorderColor = "#ccc";

    @Column(columnDefinition = "TEXT") private String avatarData;
    @Column(columnDefinition = "TEXT") private String bannerData;
    @Column(columnDefinition = "TEXT") private String suggestionsJson;

    /** URL opened when booking intent is detected (e.g. Calendly, Jane App). */
    @Column private String bookingUrl;

    // ===== Getters / Setters =====

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getHeaderColor() { return headerColor; }
    public void setHeaderColor(String v) { this.headerColor = v; }
    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String v) { this.backgroundColor = v; }
    public String getTextColor() { return textColor; }
    public void setTextColor(String v) { this.textColor = v; }
    public String getIconColor() { return iconColor; }
    public void setIconColor(String v) { this.iconColor = v; }
    public String getChipBackgroundColor() { return chipBackgroundColor; }
    public void setChipBackgroundColor(String v) { this.chipBackgroundColor = v; }
    public String getChipHoverColor() { return chipHoverColor; }
    public void setChipHoverColor(String v) { this.chipHoverColor = v; }
    public String getChipBorderColor() { return chipBorderColor; }
    public void setChipBorderColor(String v) { this.chipBorderColor = v; }
    public String getAvatarData() { return avatarData; }
    public void setAvatarData(String v) { this.avatarData = v; }
    public String getBannerData() { return bannerData; }
    public void setBannerData(String v) { this.bannerData = v; }
    public String getSuggestionsJson() { return suggestionsJson; }
    public void setSuggestionsJson(String v) { this.suggestionsJson = v; }
    public String getBookingUrl() { return bookingUrl; }
    public void setBookingUrl(String v) { this.bookingUrl = v; }
}
