package com.resumeor.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private CaptchaService captchaService;
    private CaptchaChallenge captcha;

    @BeforeEach
    void seedDemoAccountForLoginTest() {
        userService.createDemoAccount();
        captcha = captchaService.createChallenge("ABCD");
    }

    @Test
    void demoAccountCanLogin() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo123\",\"captchaId\":\"%s\",\"captchaCode\":\"ABCD\"}".formatted(captcha.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void returnsCaptchaChallenge() throws Exception {
        mockMvc.perform(get("/api/user/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.image").value(org.hamcrest.Matchers.startsWith("data:image/svg+xml;base64,")));
    }

    @Test
    void rejectsLoginWithoutCaptcha() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectsIncorrectCaptcha() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo123\",\"captchaId\":\"%s\",\"captchaCode\":\"ZZZZ\"}".formatted(captcha.id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("验证码错误或已过期，请重新获取"));
    }

    @Test
    void rejectsInvalidCredentialsWithUnauthorizedStatus() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"wrong-password\",\"captchaId\":\"%s\",\"captchaCode\":\"ABCD\"}".formatted(captcha.id())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void registersNewAccount() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new-user\",\"password\":\"new-password\",\"nickname\":\"新用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("new-user"));
    }
}
