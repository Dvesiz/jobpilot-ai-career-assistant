package com.resumeor.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiGateway {
    private final AiConfigService aiConfigService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public AiGateway(AiConfigService aiConfigService, ObjectMapper objectMapper) {
        this.aiConfigService = aiConfigService;
        this.objectMapper = objectMapper;
    }

    public String generate(long userId, String systemPrompt, String userPrompt, String fallback) {
        UserAiConfig config = aiConfigService.resolve(userId);
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            return fallback;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", config.model(),
                    "temperature", 0.35,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(chatCompletionsUrl(config.baseUrl())))
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return fallback;
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return content.isBlank() ? fallback : content;
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String chatCompletionsUrl(String baseUrl) {
        String normalized = baseUrl.trim().replaceAll("/+$", "");
        return normalized.endsWith("/chat/completions") ? normalized : normalized + "/chat/completions";
    }
}
