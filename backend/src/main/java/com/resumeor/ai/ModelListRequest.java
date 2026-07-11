package com.resumeor.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ModelListRequest(
        @NotBlank @Pattern(regexp = "https?://.+", message = "请求地址必须以 http:// 或 https:// 开头") String baseUrl,
        String apiKey
) {
}
