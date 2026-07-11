package com.resumeor.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(String baseUrl, String apiKey, String model) {
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
