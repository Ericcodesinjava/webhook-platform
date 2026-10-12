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

    public EventService(EventRepository eventRepository,
                        DeliveryAttemptRepository deliveryAttemptRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Event ingestEvent(String tenantId, String eventType, String payload) {
        Event event = new Event();
        event.setTenantId(tenantId);
        event.setEventType(eventType);
        event.setPayload(payload);
        Event saved = eventRepository.save(event);

        // Key = tenantId, so one tenant's events stay in order. Value = just the event ID.
        kafkaTemplate.send(KafkaTopicConfig.EVENTS_TOPIC, tenantId, saved.getId().toString());

        return saved;
    }

    public List<DeliveryAttempt> getDeliveryHistory(UUID eventId) {
        return deliveryAttemptRepository.findByEventIdOrderByAttemptedAtAsc(eventId);
    }
}