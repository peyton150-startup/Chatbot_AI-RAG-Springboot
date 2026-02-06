package com.harmony.chatbot.admin;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import com.harmony.chatbot.theme.Theme;
import com.harmony.chatbot.theme.ChatbotThemeService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // Admin dashboard
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity());
        model.addAttribute("theme", themeService.getThemeForCurrentUser());
        return "admin";
    }

    // Add or update a user
    @PostMapping("/users")
    public String createUser(@ModelAttribute("user") @Valid UserEntity user,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("theme", themeService.getThemeForCurrentUser());
            return "admin";
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    // Delete user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    // Edit user
    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @ModelAttribute UserEntity updatedUser) {

        UserEntity user = userService.getUserById(id).orElseThrow();

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            user.setPassword(updatedUser.getPassword());
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    // Update theme for logged-in admin
    @PostMapping("/theme")
    public String updateTheme(@ModelAttribute Theme theme,
                              @RequestParam(required = false) MultipartFile avatar) {

        try {
            themeService.updateThemeForCurrentUser(theme, avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin";
    }
}
