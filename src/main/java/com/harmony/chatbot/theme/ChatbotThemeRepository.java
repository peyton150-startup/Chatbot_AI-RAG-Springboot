package com.harmony.chatbot.theme;

import com.harmony.chatbot.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatbotThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findByUser(UserEntity user);
}
