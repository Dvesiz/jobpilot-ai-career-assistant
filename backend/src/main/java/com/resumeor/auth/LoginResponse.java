package com.resumeor.auth;

public record LoginResponse(String token, UserProfile user) {
    public record UserProfile(long id, String username, String nickname) {
    }
}
