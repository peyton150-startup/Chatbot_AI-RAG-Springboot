package com.harmony.chatbot.config;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettingsEntity, Long> {
}