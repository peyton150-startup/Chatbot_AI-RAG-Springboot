package com.harmony.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // you can add custom queries here later
}

