package com.harmony.chatbot.user;

import com.harmony.chatbot.theme.ChatbotThemeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChatbotThemeService themeService;

    public UserService(UserRepository userRepository, ChatbotThemeService themeService) {
        this.userRepository = userRepository;
        this.themeService = themeService;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getUserByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    // --- FULL DELETE including theme ---
    @Transactional
    public void deleteUser(UserEntity user) {
        // Delete associated theme first
        themeService.deleteThemeForUser(user);

        // Delete user itself
        userRepository.delete(user);
    }
}
