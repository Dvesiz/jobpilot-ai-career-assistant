package com.resumeor.ai;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {
    private static final int LIMIT_PER_MINUTE = 20;
    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final boolean redisEnabled;

    public AiRateLimiter(StringRedisTemplate redisTemplate, @Value("${app.redis.enabled:false}") boolean redisEnabled) {
        this.redisTemplate = redisTemplate;
        this.redisEnabled = redisEnabled;
    }

    public void check(long userId) {
        if (redisEnabled && checkRedis(userId)) {
            return;
        }
        long minute = Instant.now().getEpochSecond() / 60;
        Window window = windows.compute(userId, (id, current) -> current == null || current.minute != minute ? new Window(minute, 1) : current.next());
        if (window.count > LIMIT_PER_MINUTE) {
            throw new IllegalArgumentException("AI 请求过于频繁，请稍后再试");
        }
    }

    private boolean checkRedis(long userId) {
        try {
            String key = "resumeor:rate:" + userId + ":" + Instant.now().getEpochSecond() / 60;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, java.time.Duration.ofSeconds(70));
            }
            if (count != null && count > LIMIT_PER_MINUTE) {
                throw new IllegalArgumentException("AI 请求过于频繁，请稍后再试");
            }
            return true;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private record Window(long minute, int count) {
        Window next() { return new Window(minute, count + 1); }
    }
}
