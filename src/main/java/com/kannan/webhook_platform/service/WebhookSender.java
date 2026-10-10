package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class WebhookSender {

    private final RestClient restClient;

    public WebhookSender() {
        this.restClient = RestClient.create();
    }

    @CircuitBreaker(name = "webhookDelivery", fallbackMethod = "sendFallback")
    public ResponseEntity<String> send(Event event, Endpoint endpoint) {
        String signature = signPayload(event.getPayload(), endpoint.getSecret());

        return restClient.post()
                .uri(endpoint.getUrl())
                .header("X-Webhook-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event.getPayload())
                .retrieve()
                .toEntity(String.class);
    }

    // Fires on a real failed call, AND on every call rejected while the breaker is OPEN
    // (that second case arrives as a CallNotPermittedException, not a real network error).
    private ResponseEntity<String> sendFallback(Event event, Endpoint endpoint, Throwable t) {
        String message = (t instanceof CallNotPermittedException)
                ? "Circuit breaker open — endpoint temporarily skipped"
                : t.getMessage();
        throw new WebhookDeliveryException(message, t);
    }

    private String signPayload(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign payload", e);
        }
    }
}