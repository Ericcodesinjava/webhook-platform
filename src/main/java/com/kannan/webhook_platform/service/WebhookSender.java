package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
    private final CircuitBreakerRegistry registry;

    public WebhookSender(CircuitBreakerRegistry registry) {
        this.restClient = RestClient.create();
        this.registry = registry;
    }

    public ResponseEntity<String> send(Event event, Endpoint endpoint) {
        // One breaker per endpoint, created on first use from the "webhookDelivery" template
        CircuitBreaker breaker = registry.circuitBreaker("endpoint-" + endpoint.getId(), "webhookDelivery");

        try {
            return breaker.executeSupplier(() -> doSend(event, endpoint));
        } catch (CallNotPermittedException e) {
            throw new WebhookDeliveryException("Circuit breaker open — endpoint temporarily skipped", e);
        } catch (RuntimeException e) {
            throw new WebhookDeliveryException(e.getMessage(), e);
        }
    }

    private ResponseEntity<String> doSend(Event event, Endpoint endpoint) {
        String signature = signPayload(event.getPayload(), endpoint.getSecret());

        return restClient.post()
                .uri(endpoint.getUrl())
                .header("X-Webhook-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event.getPayload())
                .retrieve()
                .toEntity(String.class);
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