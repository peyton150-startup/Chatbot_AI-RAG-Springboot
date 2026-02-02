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
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> {
            String port = System.getenv("PORT");
            if (port != null) {
                factory.setPort(Integer.parseInt(port));
            }
        };
    }

    @Bean
    CommandLineRunner init(UserService userService) {
        return args -> {
            if (userService.getAllUsers().isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123"); // hashed automatically
                admin.setRole("ADMIN");         // ensure UserEntity has a role field
                userService.saveUser(admin);
                System.out.println("Default admin created: admin / admin123");
            }
        };
    }
}
