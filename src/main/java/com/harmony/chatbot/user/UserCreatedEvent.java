package com.harmony.chatbot.user;

import org.springframework.context.ApplicationEvent;

public class UserCreatedEvent extends ApplicationEvent {

    private final Long userId;

    public UserCreatedEvent(Object source, Long userId) {
        super(source); // Spring requires a source object
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    /**
     * Convenience factory method
     */
    public static UserCreatedEvent of(Object source, Long userId) {
        return new UserCreatedEvent(source, userId);
    }
}
