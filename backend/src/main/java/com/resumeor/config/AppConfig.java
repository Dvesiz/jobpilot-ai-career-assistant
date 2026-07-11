package com.resumeor.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.resumeor.ai.AiProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AiProperties.class})
public class AppConfig {
}
