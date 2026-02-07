package com.harmony.chatbot.user;

import com.harmony.chatbot.theme.ChatbotThemeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatbotThemeService themeService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ChatbotThemeService themeService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.themeService = themeService;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // TEMP ROLE NORMALIZATION (your existing fix)
        if (user.getRole() != null && user.getRole().startsWith("ROLE_")) {
            user.setRole(user.getRole().replace("ROLE_", ""));
        }

        return user;
    }

    public Optional<UserEntity> getUserByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity saveUser(UserEntity user) {
        boolean isNewUser = (user.getId() == null);

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        user.setUsername(user.getUsername().substring(0, Math.min(50, user.getUsername().length())));
        user.setEmail(user.getEmail().substring(0, Math.min(100, user.getEmail().length())));

        if (user.getPassword() != null &&
            !user.getPassword().isEmpty() &&
            !user.getPassword().startsWith("$2")) {

            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        UserEntity savedUser = userRepository.save(user);

        // ✅ AUTO-CREATE THEME ON FIRST SAVE
        if (isNewUser) {
            themeService.createDefaultThemeIfMissing(savedUser);
        }

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
