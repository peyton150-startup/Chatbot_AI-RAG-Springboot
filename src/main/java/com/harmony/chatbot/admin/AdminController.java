package com.harmony.chatbot.controller;

import com.harmony.chatbot.model.Theme;
import com.harmony.chatbot.model.User;
import com.harmony.chatbot.repository.ThemeRepository;
import com.harmony.chatbot.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ThemeRepository themeRepository;

    public AdminController(UserRepository userRepository, ThemeRepository themeRepository) {
        this.userRepository = userRepository;
        this.themeRepository = themeRepository;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        // Always provide a user object for the form (new user)
        model.addAttribute("user", new User());

        // Provide a default theme if none exists
        Theme defaultTheme = themeRepository.findById(1L).orElseGet(() -> {
            Theme theme = new Theme();
            theme.setHeaderColor("#6f42c1"); // Purple header
            theme.setBackgroundColor("#f8f9fa"); // Light background
            theme.setTextColor("#212529"); // Default dark text
            theme.setIconColor("#6f42c1"); // Purple icons
            theme.setAvatarFilename("default-avatar.png"); // Default avatar
            themeRepository.save(theme);
            return theme;
        });

        model.addAttribute("theme", defaultTheme);
        model.addAttribute("editMode", false);

        return "admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute User user) {
        if (user.getId() != null) {
            // Editing existing user
            Optional<User> existing = userRepository.findById(user.getId());
            existing.ifPresent(u -> {
                u.setUsername(user.getUsername());
                u.setEmail(user.getEmail());
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    u.setPassword(user.getPassword()); // Hash in real app
                }
                u.setRole(user.getRole());
                userRepository.save(u);
            });
        } else {
            // New user
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    public String saveTheme(@ModelAttribute Theme theme) {
        // If the theme already exists, update it
        Theme existingTheme = themeRepository.findById(theme.getId()).orElse(theme);
        existingTheme.setHeaderColor(theme.getHeaderColor());
        existingTheme.setBackgroundColor(theme.getBackgroundColor());
        existingTheme.setTextColor(theme.getTextColor());
        existingTheme.setIconColor(theme.getIconColor());
        existingTheme.setAvatarFilename(
                theme.getAvatarFilename() != null ? theme.getAvatarFilename() : existingTheme.getAvatarFilename()
        );
        themeRepository.save(existingTheme);
        return "redirect:/admin";
    }
}
