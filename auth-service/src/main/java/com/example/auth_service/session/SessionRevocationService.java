package com.example.auth_service.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionRevocationService {

    private static final String KEY_PREFIX = "revoked:session:";

    private final StringRedisTemplate redisTemplate;

    public void revoke(String sessionId, Instant expiresAt) {

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        if (expiresAt == null) {
            return;
        }

        long remainingMillis =
                expiresAt.toEpochMilli()
                        - Instant.now().toEpochMilli();

        if (remainingMillis <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(
                buildKey(sessionId),
                "true",
                Duration.ofMillis(remainingMillis)
        );
    }

    public boolean isRevoked(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(
                        buildKey(sessionId)
                )
        );
    }

    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}