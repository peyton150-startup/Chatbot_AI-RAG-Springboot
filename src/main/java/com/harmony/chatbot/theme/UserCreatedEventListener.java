package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserCreatedEvent;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventListener {

    private final ChatbotThemeRepository themeRepository;
    private final UserService userService;

    public UserCreatedEventListener(ChatbotThemeRepository themeRepository,
                                    UserService userService) {
        this.themeRepository = themeRepository;
        this.userService = userService;
    }

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        Long userId = event.getUserId();

        if (themeRepository.findByUserId(userId).isPresent()) return;

        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for theme creation"));

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
