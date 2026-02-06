package com.harmony.chatbot.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatbotThemeRepository extends JpaRepository<ChatbotThemeEntity, Long> {
    Optional<ChatbotThemeEntity> findByUserId(Long userId);
}
