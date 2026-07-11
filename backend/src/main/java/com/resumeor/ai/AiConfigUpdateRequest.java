package com.resumeor.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AiConfigUpdateRequest(
        @NotBlank @Pattern(regexp = "https?://.+", message = "模型地址必须以 http:// 或 https:// 开头") String baseUrl,
        @NotBlank String model,
        String apiKey,
        boolean clearApiKey
) {
}
