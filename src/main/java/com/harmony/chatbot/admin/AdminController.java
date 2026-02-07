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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ChatbotThemeService themeService;

    public AdminController(UserService userService,
                           ChatbotThemeService themeService) {
        this.userService = userService;
        this.themeService = themeService;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails currentUser,
                                 Model model,
                                 @ModelAttribute("success") String successMessage,
                                 @ModelAttribute("error") String errorMessage) {

        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user");
        }

        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);

        UserEntity adminUser = userService
                .getUserByUsernameOptional(currentUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("Admin not found"));

        ChatbotThemeEntity theme = themeService.getOrCreateThemeForUser(adminUser);
        model.addAttribute("theme", theme);

        // Pass flash messages to template
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);

        return "admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute UserEntity user, RedirectAttributes redirectAttributes) {

        try {
            if (user.getId() != null) {
                userService.getUserById(user.getId()).ifPresent(existing -> {
                    existing.setUsername(user.getUsername());
                    existing.setEmail(user.getEmail());
                    existing.setRole(user.getRole());

                    if (user.getPassword() != null && !user.getPassword().isBlank()) {
                        existing.setPassword(user.getPassword());
                    }

                    userService.saveUser(existing);
                });
                redirectAttributes.addFlashAttribute("success", "User updated successfully");
            } else {
                userService.saveUser(user);
                redirectAttributes.addFlashAttribute("success", "User added successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving user: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // Delete user completely (including theme)
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.getUserById(id).ifPresentOrElse(
                user -> {
                    userService.deleteUser(user);
                    redirectAttributes.addFlashAttribute("success", "User deleted successfully");
                },
                () -> redirectAttributes.addFlashAttribute("error", "User not found")
        );
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    public String saveTheme(@AuthenticationPrincipal UserDetails currentUser,
                            @ModelAttribute ChatbotThemeEntity themeForm,
                            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                            RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user");
        }

        try {
            UserEntity user = userService
                    .getUserByUsernameOptional(currentUser.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            themeService.updateThemeForUser(user, themeForm, avatarFile);
            redirectAttributes.addFlashAttribute("success", "Theme updated successfully");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating theme: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}
