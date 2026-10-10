package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.DeliveryAttempt;
import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import com.kannan.webhook_platform.repository.DeliveryAttemptRepository;
import com.kannan.webhook_platform.repository.EndpointRepository;
import com.kannan.webhook_platform.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RetryScheduler {

    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final EventRepository eventRepository;
    private final EndpointRepository endpointRepository;
    private final DeliveryService deliveryService;

    public RetryScheduler(DeliveryAttemptRepository deliveryAttemptRepository,
                          EventRepository eventRepository,
                          EndpointRepository endpointRepository,
                          DeliveryService deliveryService) {
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.eventRepository = eventRepository;
        this.endpointRepository = endpointRepository;
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedRate = 10000)
    public void processDueRetries() {
        List<DeliveryAttempt> due = deliveryAttemptRepository
                .findByStatusAndNextRetryAtLessThanEqual("FAILED", LocalDateTime.now());

        for (DeliveryAttempt attempt : due) {
            // Clear nextRetryAt immediately so this row isn't picked up again
            // by the next tick, 10 seconds from now, before the retry finishes.
            attempt.setNextRetryAt(null);
            deliveryAttemptRepository.save(attempt);

            Event event = eventRepository.findById(attempt.getEventId()).orElse(null);
            Endpoint endpoint = endpointRepository.findById(attempt.getEndpointId()).orElse(null);
            if (event == null || endpoint == null) continue; // safety: skip if deleted since

            deliveryService.attemptDelivery(event, endpoint, attempt.getAttemptNumber() + 1);
        }
    }
}