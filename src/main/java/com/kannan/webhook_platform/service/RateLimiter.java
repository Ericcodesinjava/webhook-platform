package com.kannan.webhook_platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private final StringRedisTemplate redis;
    private final int limitPerMinute;

    public RateLimiter(StringRedisTemplate redis,
                       @Value("${app.rate-limit.per-minute:100}") int limitPerMinute) {
        this.redis = redis;
        this.limitPerMinute = limitPerMinute;
    }

    /** Returns true if this tenant may send another event right now. */
    public boolean tryAcquire(String tenantId) {
        try {
            long currentMinute = System.currentTimeMillis() / 60_000;
            String key = "rate:" + tenantId + ":" + currentMinute;

            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1) {
                redis.expire(key, Duration.ofSeconds(70));
            }
            return count == null || count <= limitPerMinute;
        } catch (Exception e) {
            // Fail open: a broken rate limiter must not take the platform down
            log.warn("Rate limiter unavailable, allowing request: {}", e.getMessage());
            return true;
        }
    }
}