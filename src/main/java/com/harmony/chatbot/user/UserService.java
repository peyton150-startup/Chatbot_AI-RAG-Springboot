package com.harmony.chatbot.user;

import com.harmony.chatbot.theme.ChatbotThemeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ChatbotThemeService themeService;
    private final ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository,
                       ChatbotThemeService themeService,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.themeService = themeService;
        this.eventPublisher = eventPublisher;
    }

    public Iterable<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getUserByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity saveUser(UserEntity user) {
        boolean isNew = (user.getId() == null);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        UserEntity savedUser = userRepository.save(user);

        if (isNew) {
            eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser.getId()));
        }

        return savedUser;
    }

    @Transactional
    public void deleteUserCompletely(UserEntity user) {
        if (user == null) return;
        themeService.deleteThemeForUser(user);
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
