package com.harmony.chatbot.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all users
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by ID
     */
    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Create or update a user
     */
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Delete a user by ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Check if a user exists
     */
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}

