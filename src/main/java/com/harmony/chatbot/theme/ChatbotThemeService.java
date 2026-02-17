package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ChatbotThemeService {

    private final ChatbotThemeRepository themeRepository;

    @Autowired @Lazy
    private UserService userService;

    @Autowired
    public ChatbotThemeService(ChatbotThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Optional<ChatbotThemeEntity> getThemeEntityByUser(UserEntity user) {
        return themeRepository.findByUserId(user.getId());
    }

    public ChatbotThemeEntity getOrCreateThemeForUser(UserEntity user) {
        return themeRepository.findByUserId(user.getId()).orElseGet(() -> {
            ChatbotThemeEntity t = new ChatbotThemeEntity();
            t.setUser(user);
            t.setHeaderColor("#0d6efd");
            t.setBackgroundColor("#ffffff");
            t.setTextColor("#000000");
            t.setIconColor("#0d6efd");
            t.setChipBackgroundColor("#f0f0f0");
            t.setChipHoverColor("#e0e0e0");
            t.setChipBorderColor("#ccc");
            return themeRepository.save(t);
        });
    }

    public ChatbotThemeEntity updateThemeForUser(UserEntity user, ChatbotThemeEntity updated, MultipartFile ignored) {
        ChatbotThemeEntity theme = themeRepository.findByUserId(user.getId()).orElseGet(() -> {
            ChatbotThemeEntity t = new ChatbotThemeEntity();
            t.setUser(user);
            return t;
        });

        if (updated.getHeaderColor()        != null) theme.setHeaderColor(updated.getHeaderColor());
        if (updated.getBackgroundColor()     != null) theme.setBackgroundColor(updated.getBackgroundColor());
        if (updated.getTextColor()           != null) theme.setTextColor(updated.getTextColor());
        if (updated.getIconColor()           != null) theme.setIconColor(updated.getIconColor());
        if (updated.getChipBackgroundColor() != null) theme.setChipBackgroundColor(updated.getChipBackgroundColor());
        if (updated.getChipHoverColor()      != null) theme.setChipHoverColor(updated.getChipHoverColor());
        if (updated.getChipBorderColor()     != null) theme.setChipBorderColor(updated.getChipBorderColor());
        if (updated.getSuggestionsJson()     != null) theme.setSuggestionsJson(updated.getSuggestionsJson());
        if (updated.getBookingUrl()          != null) theme.setBookingUrl(updated.getBookingUrl());

        if (updated.getAvatarData() != null && !updated.getAvatarData().isBlank())
            theme.setAvatarData(updated.getAvatarData());
        if (updated.getBannerData() != null && !updated.getBannerData().isBlank())
            theme.setBannerData(updated.getBannerData());

        return themeRepository.save(theme);
    }

    public void deleteThemeForUser(UserEntity user) {
        getThemeEntityByUser(user).ifPresent(themeRepository::delete);
    }

    public ChatbotThemeEntity getDefaultTheme() {
        ChatbotThemeEntity t = new ChatbotThemeEntity();
        t.setHeaderColor("#0d6efd"); t.setBackgroundColor("#ffffff");
        t.setTextColor("#000000");   t.setIconColor("#0d6efd");
        t.setChipBackgroundColor("#f0f0f0"); t.setChipHoverColor("#e0e0e0");
        t.setChipBorderColor("#ccc");
        return t;
    }

    public ChatbotThemeEntity getThemeForCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            UserEntity user = userService.getUserByUsernameOptional(u.getUsername()).orElse(null);
            if (user != null) return getOrCreateThemeForUser(user);
        }
        return getDefaultTheme();
    }
}
