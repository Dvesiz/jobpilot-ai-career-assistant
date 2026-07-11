package com.resumeor.interview;

import com.resumeor.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InterviewControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void startsInterviewForOwnedResume() throws Exception {
        jdbcTemplate.update("INSERT INTO user_resume (user_id, original_content) VALUES (?, ?)", 1L, "产品设计项目经历");
        Long resumeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM user_resume", Long.class);
        String body = "{\"resumeId\":" + resumeId + ",\"jobName\":\"产品设计师\",\"jobJd\":\"负责产品设计与用户研究\"}";
        mockMvc.perform(post("/api/interview/start").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.question").isNotEmpty());
    }

    @Test
    void createsFollowUpForAnsweredQuestion() throws Exception {
        jdbcTemplate.update("INSERT INTO interview_record (user_id, resume_id, job_name, question, user_answer, ai_comment) VALUES (?, ?, ?, ?, ?, ?)",
                1L, 1L, "产品设计师", "请介绍一个项目", "我负责了需求梳理与原型设计。", "请补充量化成果。");
        Long recordId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM interview_record", Long.class);
        mockMvc.perform(post("/api/interview/follow-up/{recordId}", recordId).header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.question").isNotEmpty());
    }

    private String token() { return "Bearer " + jwtService.createToken(1L, "demo"); }
}
