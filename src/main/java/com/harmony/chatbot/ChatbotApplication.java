package com.harmony.chatbot;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.stream.StreamSupport;

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
            if (port != null) factory.setPort(Integer.parseInt(port));
        };
    }

    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            String adminUsername = "admin";
            boolean exists = StreamSupport.stream(userService.getAllUsers().spliterator(), false)
                    .anyMatch(u -> u.getUsername().equals(adminUsername));

            if (!exists) {
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123");
                admin.setRole("ROLE_ADMIN");
                userService.saveUser(admin);
            }
        };
    }
}
