package com.resumeor.interview;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class InterviewRepository {
    private final JdbcTemplate jdbcTemplate;

    public InterviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long create(long userId, long resumeId, String jobName, String question) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO interview_record (user_id, resume_id, job_name, question) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            statement.setLong(1, userId);
            statement.setLong(2, resumeId);
            statement.setString(3, jobName);
            statement.setString(4, question);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("面试记录保存失败");
        }
        return key.longValue();
    }

    public void updateAnswer(long id, long userId, String answer, String comment, String standardAnswer) {
        jdbcTemplate.update(
                "UPDATE interview_record SET user_answer = ?, ai_comment = ?, standard_answer = ? WHERE id = ? AND user_id = ?",
                answer, comment, standardAnswer, id, userId
        );
    }

    public Optional<InterviewRecord> findOwned(long id, long userId) {
        return jdbcTemplate.query("SELECT * FROM interview_record WHERE id = ? AND user_id = ?", this::map, id, userId).stream().findFirst();
    }

    public List<InterviewRecord> findAll(long userId) {
        return jdbcTemplate.query("SELECT * FROM interview_record WHERE user_id = ? ORDER BY create_time DESC", this::map, userId);
    }

    private InterviewRecord map(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new InterviewRecord(
                resultSet.getLong("id"), resultSet.getLong("resume_id"), resultSet.getString("job_name"),
                resultSet.getString("question"), resultSet.getString("user_answer"), resultSet.getString("ai_comment"),
                resultSet.getString("standard_answer"), resultSet.getObject("create_time", LocalDateTime.class)
        );
    }
}
