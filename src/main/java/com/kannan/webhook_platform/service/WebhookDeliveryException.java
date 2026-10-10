package com.kannan.webhook_platform.service;

public class WebhookDeliveryException extends RuntimeException {
    public WebhookDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}