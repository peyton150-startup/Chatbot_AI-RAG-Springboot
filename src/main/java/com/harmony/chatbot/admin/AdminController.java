package com.harmony.chatbot.admin;

import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ChatbotThemeService themeService;

    public AdminController(UserService userService, ChatbotThemeService themeService) {
        this.userService = userService;
        this.themeService = themeService;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        // Load all users for admin table
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);

        // New user object for Add/Edit form
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);

        // Load current user's theme
        ChatbotThemeEntity theme = themeService.getThemeForCurrentUser();
        model.addAttribute("theme", theme);

        return "admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute UserEntity user) {
        if (user.getId() != null) {
            // Editing existing user
            userService.getUserById(user.getId()).ifPresent(existing -> {
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    existing.setPassword(user.getPassword()); // hash automatically in UserService
                }
                existing.setRole(user.getRole());
                userService.saveUser(existing);
            });
        } else {
            // New user
            userService.saveUser(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    public String saveTheme(@RequestParam(required = false) MultipartFile avatarFile,
                            @AuthenticationPrincipal UserDetails currentUser,
                            @ModelAttribute ChatbotThemeEntity themeForm) throws IOException {

        ChatbotThemeEntity updatedTheme = themeService.updateTheme(themeForm, avatarFile);
        return "redirect:/admin";
    }

    @PostMapping("/theme/user/{userId}")
    public String saveThemeForUser(@PathVariable Long userId,
                                   @RequestParam(required = false) MultipartFile avatarFile,
                                   @ModelAttribute ChatbotThemeEntity themeForm) throws IOException {

        themeService.updateThemeForUser(userId, themeForm, avatarFile);
        return "redirect:/admin";
    }
}
