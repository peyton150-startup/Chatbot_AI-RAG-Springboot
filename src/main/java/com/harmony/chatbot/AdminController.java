package com.harmony.chatbot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPage(Model model) {
        // Example: pass a simple list of users to the template
        model.addAttribute("users", UserRepository.getUsers()); // placeholder
        return "admin"; // maps to templates/admin.html
    }
}
