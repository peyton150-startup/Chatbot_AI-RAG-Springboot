package com.harmony.chatbot.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Create or update a user
     */
    public UserEntity saveUser(UserEntity user) {

        // Only hash if password is new or changed
        if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }

        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Check existence
     */
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
