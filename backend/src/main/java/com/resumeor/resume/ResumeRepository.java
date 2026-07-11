package com.resumeor.resume;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public class ResumeRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResumeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ResumeDetail> findOwned(long id, long userId) {
        return jdbcTemplate.query(
                "SELECT id, original_content, optimized_content, job_jd, match_score, match_report FROM user_resume WHERE id = ? AND user_id = ?",
                (resultSet, rowNumber) -> new ResumeDetail(
                        resultSet.getLong("id"),
                        resultSet.getString("original_content"),
                        resultSet.getString("optimized_content"),
                        resultSet.getString("job_jd"),
                        (Integer) resultSet.getObject("match_score"),
                        resultSet.getString("match_report")
                ),
                id, userId
        ).stream().findFirst();
    }

    public void updateOptimizedContent(long id, long userId, String content) {
        jdbcTemplate.update("UPDATE user_resume SET optimized_content = ? WHERE id = ? AND user_id = ?", content, id, userId);
    }

    public void updateMatch(long id, long userId, String jobJd, int score, String report) {
        jdbcTemplate.update("UPDATE user_resume SET job_jd = ?, match_score = ?, match_report = ? WHERE id = ? AND user_id = ?", jobJd, score, report, id, userId);
    }

    public List<ResumeSummary> findAll(long userId) {
        return jdbcTemplate.query(
                "SELECT id, file_name, match_score, create_time FROM user_resume WHERE user_id = ? ORDER BY create_time DESC",
                (resultSet, rowNumber) -> new ResumeSummary(
                        resultSet.getLong("id"), resultSet.getString("file_name"),
                        (Integer) resultSet.getObject("match_score"),
                        resultSet.getObject("create_time", java.time.LocalDateTime.class)
                ),
                userId
        );
    }

    public boolean deleteOwned(long id, long userId) {
        return jdbcTemplate.update("DELETE FROM user_resume WHERE id = ? AND user_id = ?", id, userId) > 0;
    }
}
