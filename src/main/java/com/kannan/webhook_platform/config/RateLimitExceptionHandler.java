package com.kannan.webhook_platform.config;

import com.kannan.webhook_platform.service.RateLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handle(RateLimitExceededException ex) {
        return ResponseEntity.status(429)
                .header("Retry-After", "60")
                .body(Map.of("error", "Too Many Requests", "message", ex.getMessage()));
    }
}