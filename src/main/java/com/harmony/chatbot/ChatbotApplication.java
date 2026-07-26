package com.harmony.chatbot;

import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatbotApplication {
    private static final Logger log = LoggerFactory.getLogger(ChatbotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
        System.out.println("ChatbotApplication started");
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            String port = System.getenv("PORT");
            if (port != null)
                factory.setPort(Integer.parseInt(port));
        };
    }

    @Bean
    CommandLineRunner initAdmin(
            UserService userService,
            @Value("${app.bootstrap-admin.username:}") String adminUsername,
            @Value("${app.bootstrap-admin.email:}") String adminEmail,
            @Value("${app.bootstrap-admin.password:}") String adminPassword) {
        return args -> {
            if (adminUsername.isBlank() && adminEmail.isBlank() && adminPassword.isBlank()) return;
            if (adminUsername.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()) {
                throw new IllegalStateException("All BOOTSTRAP_ADMIN_* values must be set together");
            }
            if (userService.getUserByUsernameOptional(adminUsername).isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(adminPassword);
                admin.setRole("ADMIN");
                userService.saveUser(admin);
                log.info("Bootstrap administrator created for '{}'. Remove BOOTSTRAP_ADMIN_* now.", adminUsername);
            }
        };
    }
}