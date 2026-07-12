package com.resumeor.auth;

import com.resumeor.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

@RestController
@RequestMapping("/api/user")
public class AuthController {
    private final JwtService jwtService;
    private final UserService userService;
    private final CaptchaService captchaService;

    public AuthController(JwtService jwtService, UserService userService, CaptchaService captchaService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaChallenge> captcha() {
        return ApiResponse.success(captchaService.createChallenge());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        captchaService.verifyAndConsume(request.captchaId(), request.captchaCode());
        return ApiResponse.success(userService.login(request, jwtService));
    }

    @PostMapping("/register")
    public ApiResponse<UserProfile> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfile> profile(@RequestAttribute long userId) {
        return ApiResponse.success(userService.profile(userId));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request, @RequestAttribute long userId) {
        userService.changePassword(userId, request);
        return ApiResponse.success(null);
    }
}
