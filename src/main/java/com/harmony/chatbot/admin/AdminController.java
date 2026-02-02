package com.harmony.chatbot.admin;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Admin dashboard – list users
     */
   @GetMapping
public String adminHome(Model model) {
    var users = userService.getAllUsers();

    // Strip passwords before sending to template
    users.forEach(u -> u.setPassword(null));

    model.addAttribute("users", users);
    return "admin";
}


    /**
     * Create a new user (basic version)
     */
    @PostMapping("/users")
    public String createUser(@ModelAttribute UserEntity user) {
        userService.saveUser(user);
        return "redirect:/admin";
    }

    /**
     * Delete a user
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
