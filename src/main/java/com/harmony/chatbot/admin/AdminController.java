package com.harmony.chatbot.controller;

import com.harmony.chatbot.theme.Theme;
import com.harmony.chatbot.theme.ChatbotThemeRepository;  // Updated import
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ChatbotThemeRepository themeRepository; // Updated reference

    public AdminController(UserRepository userRepository, ChatbotThemeRepository themeRepository) {
        this.userRepository = userRepository;
        this.themeRepository = themeRepository;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        // Load all users for admin table
        List<UserEntity> users = userRepository.findAll();
        model.addAttribute("users", users);

        // New user object for Add/Edit form
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);

        // Load current user's theme or create a default one
        UserEntity user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow();
        Theme theme = themeRepository.findByUser(user).orElseGet(() -> {
            Theme defaultTheme = new Theme();
            defaultTheme.setUser(user);
            defaultTheme.setHeaderColor("#b46a8c");       // default purple
            defaultTheme.setBackgroundColor("#ffffff");  // default white
            defaultTheme.setTextColor("#000000");        // default black
            defaultTheme.setIconColor("#b46a8c");        // match header
            defaultTheme.setAvatarFilename("default-avatar.png");
            themeRepository.save(defaultTheme);
            return defaultTheme;
        });

        model.addAttribute("theme", theme);

        return "admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute UserEntity user) {
        if (user.getId() != null) {
            // Editing existing user
            userRepository.findById(user.getId()).ifPresent(existing -> {
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    existing.setPassword(user.getPassword()); // hash in production
                }
                existing.setRole(user.getRole());
                userRepository.save(existing);
            });
        } else {
            // New user
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    public String saveTheme(@AuthenticationPrincipal UserDetails currentUser,
                            @ModelAttribute Theme themeForm) {

        UserEntity user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow();
        Theme theme = themeRepository.findByUser(user).orElse(themeForm);

        theme.setHeaderColor(themeForm.getHeaderColor());
        theme.setBackgroundColor(themeForm.getBackgroundColor());
        theme.setTextColor(themeForm.getTextColor());
        theme.setIconColor(themeForm.getIconColor());
        if (themeForm.getAvatarFilename() != null && !themeForm.getAvatarFilename().isBlank()) {
            theme.setAvatarFilename(themeForm.getAvatarFilename());
        }
        theme.setUser(user);
        themeRepository.save(theme);

        return "redirect:/admin";
    }
}
