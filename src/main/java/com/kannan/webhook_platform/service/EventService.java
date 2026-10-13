package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.config.KafkaTopicConfig;
import com.kannan.webhook_platform.entity.DeliveryAttempt;
import com.kannan.webhook_platform.entity.Event;
import com.kannan.webhook_platform.repository.DeliveryAttemptRepository;
import com.kannan.webhook_platform.repository.EventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RateLimiter rateLimiter;                                   // NEW

    public EventService(EventRepository eventRepository,
                        DeliveryAttemptRepository deliveryAttemptRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        RateLimiter rateLimiter) {                           // NEW
        this.eventRepository = eventRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.rateLimiter = rateLimiter;                                      // NEW
    }

    public Event ingestEvent(String tenantId, String eventType, String payload) {
        if (!rateLimiter.tryAcquire(tenantId)) {                             // NEW
            throw new RateLimitExceededException(tenantId);                  // NEW
        }                                                                    // NEW

        Event event = new Event();
        event.setTenantId(tenantId);
        event.setEventType(eventType);
        event.setPayload(payload);
        Event saved = eventRepository.save(event);

        kafkaTemplate.send(KafkaTopicConfig.EVENTS_TOPIC, tenantId, saved.getId().toString());

        return saved;
    }

    public List<DeliveryAttempt> getDeliveryHistory(UUID eventId) {
        return deliveryAttemptRepository.findByEventIdOrderByAttemptedAtAsc(eventId);
    }
}