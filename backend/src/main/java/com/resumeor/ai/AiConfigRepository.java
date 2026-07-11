package com.resumeor.ai;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AiConfigRepository {
    private final JdbcTemplate jdbcTemplate;

    public AiConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<StoredConfig> findByUserId(long userId) {
        return jdbcTemplate.query("SELECT base_url, model, api_key_cipher FROM user_ai_config WHERE user_id = ?",
                (resultSet, rowNumber) -> new StoredConfig(resultSet.getString("base_url"), resultSet.getString("model"), resultSet.getString("api_key_cipher")), userId).stream().findFirst();
    }

    public void save(long userId, String baseUrl, String model, String apiKeyCipher) {
        if (findByUserId(userId).isPresent()) {
            jdbcTemplate.update("UPDATE user_ai_config SET base_url = ?, model = ?, api_key_cipher = ?, update_time = CURRENT_TIMESTAMP WHERE user_id = ?", baseUrl, model, apiKeyCipher, userId);
            return;
        }
        jdbcTemplate.update("INSERT INTO user_ai_config (user_id, base_url, model, api_key_cipher) VALUES (?, ?, ?, ?)", userId, baseUrl, model, apiKeyCipher);
    }

    public record StoredConfig(String baseUrl, String model, String apiKeyCipher) {
    }
}
