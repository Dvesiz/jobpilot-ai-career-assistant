package com.resumeor.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT id, username, password, nickname FROM sys_user WHERE username = ?",
                (resultSet, rowNumber) -> new UserAccount(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("nickname")
                ),
                username
        ).stream().findFirst();
    }

    public Optional<UserAccount> findById(long id) {
        return jdbcTemplate.query(
                "SELECT id, username, password, nickname FROM sys_user WHERE id = ?",
                (resultSet, rowNumber) -> new UserAccount(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("nickname")),
                id
        ).stream().findFirst();
    }

    public void create(String username, String password, String nickname) {
        jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, nickname) VALUES (?, ?, ?)",
                username, password, nickname
        );
    }

    public void updatePassword(long id, String password) {
        jdbcTemplate.update("UPDATE sys_user SET password = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?", password, id);
    }
}
