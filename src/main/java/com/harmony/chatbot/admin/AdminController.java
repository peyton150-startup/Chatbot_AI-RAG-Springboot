package com.harmony.chatbot.admin;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import com.harmony.chatbot.theme.ChatbotThemeEntity;
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

    // ==========================
    // ADMIN DASHBOARD
    // ==========================
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity());          // add form
        model.addAttribute("editMode", false);                 // add vs edit
        model.addAttribute("theme", themeService.getTheme());  // chatbot styling
        return "admin";
    }

    // ==========================
    // LOAD USER INTO EDIT FORM
    // ==========================
    @GetMapping("/users/{id}/edit")
    public String loadEditUser(@PathVariable Long id, Model model) {

        UserEntity user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // NEVER prefill passwords
        user.setPassword("");

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", user);
        model.addAttribute("editMode", true);
        model.addAttribute("theme", themeService.getTheme());

        return "admin";
    }

    // ==========================
    // CREATE OR UPDATE USER
    // ==========================
    @PostMapping("/users")
    public String saveUser(@ModelAttribute("user") @Valid UserEntity user,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("editMode", user.getId() != null);
            model.addAttribute("theme", themeService.getTheme());
            return "admin";
        }

        try {
            userService.saveUser(user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("editMode", user.getId() != null);
            model.addAttribute("theme", themeService.getTheme());
            return "admin";
        }

        return "redirect:/admin";
    }

    // ==========================
    // DELETE USER
    // ==========================
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    // ==========================
    // UPDATE CHATBOT THEME
    // ==========================
    @PostMapping("/theme")
    public String updateTheme(@ModelAttribute ChatbotThemeEntity theme,
                              @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                              Model model) {

        try {
            themeService.updateTheme(theme, avatar);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Theme update failed: " + e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("user", new UserEntity());
            model.addAttribute("editMode", false);
            model.addAttribute("theme", themeService.getTheme());
            return "admin";
        }

        return "redirect:/admin";
    }
}
