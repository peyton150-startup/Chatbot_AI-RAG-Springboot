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

    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity()); // for Add form
        model.addAttribute("editUser", new UserEntity()); // for Edit modal
        return "admin";
    }

    // Create new user
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

    // Delete user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    // Update user
    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @ModelAttribute("editUser") @Valid UserEntity updatedUser,
                           BindingResult bindingResult,
                           Model model) {

        if (!userService.userExists(id)) {
            model.addAttribute("errorMessage", "User not found");
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        try {
            UserEntity user = userService.getUserById(id).orElseThrow();
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            if (!updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword()); // hashed in service
            }
            userService.saveUser(user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            return "admin";
        }

        return "redirect:/admin";
    }
}
