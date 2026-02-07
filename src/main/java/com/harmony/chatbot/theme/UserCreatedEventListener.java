package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserCreatedEvent;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventListener {

    private final ChatbotThemeRepository themeRepository;

    public UserCreatedEventListener(ChatbotThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        Long userId = event.getUserId();

        if (themeRepository.findByUserId(userId).isPresent()) return;

        // Get user from event's source if possible
        Object source = event.getSource();
        UserEntity user;
        if (source instanceof UserService) {
            user = ((UserService) source).getUserById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
        } else {
            throw new IllegalStateException("Cannot retrieve UserService from event source");
        }

        ChatbotThemeEntity theme = new ChatbotThemeEntity();
        theme.setUser(user);
        theme.setHeaderColor("#0d6efd");
        theme.setBackgroundColor("#ffffff");
        theme.setTextColor("#000000");
        theme.setIconColor("#0d6efd");

        themeRepository.save(theme);

        System.out.println("✅ Default theme created for userId=" + userId);
    }
}
