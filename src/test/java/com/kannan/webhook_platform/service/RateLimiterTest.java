package com.kannan.webhook_platform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Test
    void allowsRequestUnderTheLimit() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(3L);

        RateLimiter limiter = new RateLimiter(redis, 3);

        assertThat(limiter.tryAcquire("t1")).isTrue();
    }

    @Test
    void blocksRequestOverTheLimit() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(4L);

        RateLimiter limiter = new RateLimiter(redis, 3);

        assertThat(limiter.tryAcquire("t1")).isFalse();
    }

    @Test
    void failsOpenWhenRedisIsDown() {
        when(redis.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        RateLimiter limiter = new RateLimiter(redis, 3);

        assertThat(limiter.tryAcquire("t1")).isTrue();
    }
}