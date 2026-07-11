package com.resumeor.config;

import com.resumeor.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Filter 由 Spring 容器自动注册，保留 WebMvcConfigurer 作为后续跨域与接口拦截配置入口。
    }
}
