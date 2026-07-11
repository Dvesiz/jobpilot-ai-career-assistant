package com.resumeor.config;

import com.resumeor.auth.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    ApplicationRunner seedDemoUser(UserService userService) {
        return arguments -> userService.createDemoAccount();
    }
}
