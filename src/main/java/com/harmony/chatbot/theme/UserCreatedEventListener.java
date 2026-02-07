package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserCreatedEvent;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventListener {

    private final ChatbotThemeRepository themeRepository;
    private final UserRepository userRepository;

    public UserCreatedEventListener(ChatbotThemeRepository themeRepository,
                                    UserRepository userRepository) {
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        Long userId = event.getUserId();

        if (themeRepository.findByUserId(userId).isPresent()) return;

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for theme creation"));

        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setUser(user);
        theme.setHeaderColor("#0d6efd");
        theme.setBackgroundColor("#ffffff");
        theme.setTextColor("#000000");
        theme.setIconColor("#0d6efd");
        theme.setChipBackgroundColor("#f0f0f0");
        theme.setChipHoverColor("#e0e0e0");
        theme.setChipBorderColor("#ccc");

        themeRepository.save(theme);
        System.out.println("✅ Default theme created for userId=" + userId);
    }
}
