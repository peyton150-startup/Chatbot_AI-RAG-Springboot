package com.harmony.chatbot;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
        System.out.println("ChatbotApplication started");
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            String port = System.getenv("PORT");
            if (port != null) {
                factory.setPort(Integer.parseInt(port));
                System.out.println("Setting server port from environment: " + port);
            }
        };
    }

    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            String adminUsername = "admin";
            if (userService.getAllUsers().stream().noneMatch(u -> u.getUsername().equals(adminUsername))) {
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123"); // hashed in UserService
                admin.setRole("ADMIN");
                userService.saveUser(admin);
                System.out.println("Admin user created: username=admin, password=admin123");
            } else {
                System.out.println("Admin user already exists");
            }
        };
    }
}
