package com.harmony.chatbot.user;

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

    // Constructor injection
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity saveUser(UserEntity user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty");
        if (user.getEmail() == null || user.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");
        if (user.getPassword() == null || user.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Password cannot be empty");

        user.setUsername(user.getUsername().substring(0, Math.min(50, user.getUsername().length())));
        user.setEmail(user.getEmail().substring(0, Math.min(100, user.getEmail().length())));

        // Only hash password if not already hashed
        if (!user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
