package com.resumeor.dashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {
    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardSummary summary(long userId) {
        Integer resumeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_resume WHERE user_id = ?", Integer.class, userId);
        Integer matchScore = jdbcTemplate.query(
                "SELECT match_score FROM user_resume WHERE user_id = ? AND match_score IS NOT NULL ORDER BY create_time DESC LIMIT 1",
                (resultSet, rowNumber) -> (Integer) resultSet.getObject("match_score"), userId
        ).stream().findFirst().orElse(null);
        Integer interviewCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM interview_record WHERE user_id = ? AND user_answer IS NOT NULL AND create_time >= ?",
                Integer.class, userId, LocalDate.now().withDayOfMonth(1).atStartOfDay()
        );
        if (resumeCount == null || resumeCount == 0) {
            return new DashboardSummary(0, null, interviewCount == null ? 0 : interviewCount, "上传一份简历", "/resume");
        }
        if (matchScore == null) {
            return new DashboardSummary(resumeCount, null, interviewCount == null ? 0 : interviewCount, "完成岗位匹配", "/matching");
        }
        return new DashboardSummary(resumeCount, matchScore, interviewCount == null ? 0 : interviewCount, "开始模拟面试", "/interview");
    }
}
