package com.harmony.chatbot.admin;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.theme.ChatbotThemeEntity;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
    public String adminHome(Model model, Authentication auth) {
        UserEntity currentAdmin = userService.getUserByUsername(auth.getName())
                                             .orElseThrow();

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity());

        // Load theme for current admin
        model.addAttribute("theme", themeService.getThemeForUser(currentAdmin.getId()));
        return "admin";
    }

    // Create or edit user
    @PostMapping("/users")
    public String createUser(@ModelAttribute("user") @Valid UserEntity user,
                             BindingResult result,
                             Model model,
                             Authentication auth) {

        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("theme", themeService.getThemeForUser(
                    userService.getUserByUsername(auth.getName()).orElseThrow().getId()));
            return "admin";
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

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

    // Update chatbot theme for current admin
    @PostMapping("/theme")
    public String updateTheme(@ModelAttribute ChatbotThemeEntity theme,
                              @RequestParam(required = false) MultipartFile avatar,
                              Authentication auth) {

        try {
            UserEntity currentAdmin = userService.getUserByUsername(auth.getName())
                                                 .orElseThrow();
            themeService.updateThemeForUser(currentAdmin.getId(), theme, avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin";
    }
}
