package com.kannan.webhook_platform.service;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String tenantId) {
        super("Rate limit exceeded for tenant " + tenantId + ". Try again in a minute.");
    }
}