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

    // Admin page (default = add mode)
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);
        return "admin";
    }

    // LOAD USER INTO FORM (autofill)
    @GetMapping("/users/{id}/edit")
    public String loadEditUser(@PathVariable Long id, Model model) {

        UserEntity user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(""); // never prefill password

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", user);
        model.addAttribute("editMode", true);

        return "admin";
    }

    // CREATE OR UPDATE (same endpoint)
    @PostMapping("/users")
    public String saveUser(@ModelAttribute("user") @Valid UserEntity user,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("editMode", user.getId() != null);
            return "admin";
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    // DELETE USER
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
