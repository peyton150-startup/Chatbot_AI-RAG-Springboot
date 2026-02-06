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
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);

        UserEntity user = userService.getUserByUsernameOptional(currentUser.getUsername()).orElseThrow();
        ChatbotThemeEntity theme = themeService.getThemeForUser(user.getId())
                .orElseGet(() -> themeService.getThemeForCurrentUser());
        model.addAttribute("theme", theme);

        return "admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute UserEntity user) {
        if (user.getId() != null) {
            userService.getUserById(user.getId()).ifPresent(existing -> {
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    existing.setPassword(user.getPassword());
                }
                existing.setRole(user.getRole());
                userService.saveUser(existing);
            });
        } else {
            userService.saveUser(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    public String saveTheme(@AuthenticationPrincipal UserDetails currentUser,
                            @ModelAttribute ChatbotThemeEntity themeForm,
                            @RequestParam(name = "avatar", required = false) MultipartFile avatarFile) throws IOException {
        UserEntity user = userService.getUserByUsernameOptional(currentUser.getUsername()).orElseThrow();
        themeService.updateThemeForUser(user.getId(), themeForm, avatarFile);
        return "redirect:/admin";
    }
}
