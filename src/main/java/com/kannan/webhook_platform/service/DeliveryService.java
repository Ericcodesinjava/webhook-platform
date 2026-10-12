package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.DeliveryAttempt;
import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import com.kannan.webhook_platform.repository.DeliveryAttemptRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int[] BACKOFF_SECONDS = {30, 120, 600, 1800};

    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final WebhookSender webhookSender;

    public DeliveryService(DeliveryAttemptRepository deliveryAttemptRepository, WebhookSender webhookSender) {
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.webhookSender = webhookSender;
    }

    public void attemptDelivery(Event event, Endpoint endpoint, int attemptNumber) {
        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setEventId(event.getId());
        attempt.setEndpointId(endpoint.getId());
        attempt.setAttemptNumber(attemptNumber);

        try {
            ResponseEntity<String> response = webhookSender.send(event, endpoint);
            attempt.setStatus("SUCCESS");
            attempt.setResponseStatusCode(response.getStatusCode().value());

        } catch (Exception e) {
            attempt.setErrorMessage(e.getMessage());

            if (attemptNumber >= MAX_ATTEMPTS) {
                attempt.setStatus("DEAD_LETTERED");
            } else {
                attempt.setStatus("FAILED");
                int waitSeconds = BACKOFF_SECONDS[attemptNumber - 1];
                attempt.setNextRetryAt(LocalDateTime.now().plusSeconds(waitSeconds));
            }
        }

        deliveryAttemptRepository.save(attempt);
    }
}