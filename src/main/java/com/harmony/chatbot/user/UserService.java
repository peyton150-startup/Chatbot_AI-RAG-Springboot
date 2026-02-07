package com.harmony.chatbot.user;

import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<UserEntity> getUserByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity saveUser(UserEntity user) {
        if (user.getPassword() != null &&
            !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        UserEntity saved = userRepository.save(user);

        // Publish event AFTER saving user
        eventPublisher.publishEvent(UserCreatedEvent.of(this, saved.getId()));

        return saved;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
