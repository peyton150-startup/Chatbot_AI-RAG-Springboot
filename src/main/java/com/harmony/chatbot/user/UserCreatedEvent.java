package com.harmony.chatbot.user;

public class UserCreatedEvent {

    private final Long userId;
    private final Object source;

    public UserCreatedEvent(Object source, Long userId) {
        this.source = source;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Object getSource() {
        return source;
    }

    public static UserCreatedEvent of(Object source, Long userId) {
        return new UserCreatedEvent(source, userId);
    }
}
