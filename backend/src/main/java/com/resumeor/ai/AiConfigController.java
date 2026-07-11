package com.resumeor.ai;

import com.resumeor.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/config")
public class AiConfigController {
    private final AiConfigService service;

    public AiConfigController(AiConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<AiConfigResponse> get(@RequestAttribute long userId) {
        return ApiResponse.success(service.get(userId));
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestAttribute long userId, @Valid @RequestBody AiConfigUpdateRequest request) {
        service.update(userId, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/models")
    public ApiResponse<java.util.List<String>> models(@RequestAttribute long userId, @Valid @RequestBody ModelListRequest request) {
        return ApiResponse.success(service.listModels(userId, request));
    }
}
