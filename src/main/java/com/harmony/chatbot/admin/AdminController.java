package com.harmony.chatbot.admin;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Dashboard: show all users and form for adding new user
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity()); // for add new user form
        return "admin";
    }

    // Load user for editing
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserEntity user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    // Add new user
    @PostMapping("/users")
    public String createUser(@ModelAttribute("user") @Valid UserEntity user,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        try {
            userService.saveUser(user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        return "redirect:/admin";
    }

    // Update existing user
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") @Valid UserEntity updatedUser,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        UserEntity existingUser = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(updatedUser.getPassword()); // hash inside service
        }

        userService.saveUser(existingUser);
        return "redirect:/admin";
    }

    // Delete user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
