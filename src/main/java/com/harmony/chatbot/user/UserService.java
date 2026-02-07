package com.harmony.chatbot.user;

import com.harmony.chatbot.theme.ChatbotThemeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChatbotThemeService themeService;

    public UserService(UserRepository userRepository, ChatbotThemeService themeService) {
        this.userRepository = userRepository;
        this.themeService = themeService;
    }

    public Iterable<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public java.util.Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public java.util.Optional<UserEntity> getUserByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserCompletely(UserEntity user) {
        if (user == null) return;

        // 1. Delete theme and avatar files
        themeService.deleteThemeForUser(user);

        // 2. Delete the user
        userRepository.delete(user);
    }
}
