package com.resumeor.ai;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiConfigService {
    private final AiConfigRepository repository;
    private final SecretCipher cipher;
    private final AiProperties defaults;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public AiConfigService(AiConfigRepository repository, SecretCipher cipher, AiProperties defaults, ObjectMapper objectMapper) {
        this.repository = repository;
        this.cipher = cipher;
        this.defaults = defaults;
        this.objectMapper = objectMapper;
    }

    public AiConfigResponse get(long userId) {
        return repository.findByUserId(userId)
                .map(config -> new AiConfigResponse(config.baseUrl(), config.model(), config.apiKeyCipher() != null && !config.apiKeyCipher().isBlank()))
                .orElseGet(() -> new AiConfigResponse(defaults.baseUrl(), defaults.model(), defaults.isConfigured()));
    }

    public void update(long userId, AiConfigUpdateRequest request) {
        AiConfigRepository.StoredConfig current = repository.findByUserId(userId).orElse(null);
        String apiKeyCipher;
        if (request.clearApiKey()) {
            apiKeyCipher = null;
        } else if (request.apiKey() != null && !request.apiKey().isBlank()) {
            apiKeyCipher = cipher.encrypt(request.apiKey().trim());
        } else {
            apiKeyCipher = current == null ? null : current.apiKeyCipher();
        }
        repository.save(userId, request.baseUrl().trim(), request.model().trim(), apiKeyCipher);
    }

    public UserAiConfig resolve(long userId) {
        return repository.findByUserId(userId)
                .map(config -> new UserAiConfig(config.baseUrl(), config.model(), config.apiKeyCipher() == null ? "" : cipher.decrypt(config.apiKeyCipher())))
                .orElseGet(() -> new UserAiConfig(defaults.baseUrl(), defaults.model(), defaults.apiKey()));
    }

    public List<String> listModels(long userId, ModelListRequest request) {
        String apiKey = request.apiKey() == null || request.apiKey().isBlank() ? resolve(userId).apiKey() : request.apiKey().trim();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("请先输入 API Key，或先保存已有配置");
        }
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(modelsUrl(request.baseUrl())))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalArgumentException("模型服务返回 " + response.statusCode() + "，请检查请求地址和 API Key");
            }
            JsonNode data = objectMapper.readTree(response.body()).path("data");
            List<String> models = new ArrayList<>();
            for (JsonNode item : data) {
                String id = item.path("id").asText();
                if (!id.isBlank()) models.add(id);
            }
            if (models.isEmpty()) throw new IllegalArgumentException("模型服务没有返回可用模型");
            return models.stream().sorted().toList();
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("无法获取模型列表，请确认该服务兼容 /models 接口");
        }
    }

    private String modelsUrl(String baseUrl) {
        String normalized = baseUrl.trim().replaceAll("/+$", "");
        int completionIndex = normalized.indexOf("/chat/completions");
        return completionIndex >= 0 ? normalized.substring(0, completionIndex) + "/models" : normalized + "/models";
    }
}
