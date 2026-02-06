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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user by username: " + username);
        return userRepository.findByUsername(username)
                .map(u -> {
                    System.out.println("User found: " + u.getUsername() + ", role: " + u.getRole());
                    return u;
                })
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    public Optional<UserEntity> getUserByUsernameOptional(String username) {
        System.out.println("Checking optional user: " + username);
        return userRepository.findByUsername(username);
    }

    public List<UserEntity> getAllUsers() {
        System.out.println("Retrieving all users");
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        System.out.println("Getting user by ID: " + id);
        return userRepository.findById(id);
    }

    public UserEntity saveUser(UserEntity user) {
        System.out.println("Saving user: " + user.getUsername());
        if (user.getUsername() == null || user.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty");
        if (user.getEmail() == null || user.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");

        user.setUsername(user.getUsername().substring(0, Math.min(50, user.getUsername().length())));
        user.setEmail(user.getEmail().substring(0, Math.min(100, user.getEmail().length())));

        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2")) {
            System.out.println("Encoding password for user: " + user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        System.out.println("Deleting user ID: " + id);
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
