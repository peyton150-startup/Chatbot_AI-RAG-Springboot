package com.harmony.chatbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        System.out.println("Rendering login page");
        return "login"; // login.html
    }
}
