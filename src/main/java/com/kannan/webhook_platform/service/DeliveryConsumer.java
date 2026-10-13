package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.config.KafkaTopicConfig;
import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import com.kannan.webhook_platform.repository.EndpointRepository;
import com.kannan.webhook_platform.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class DeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryConsumer.class);

    private final EventRepository eventRepository;
    private final EndpointRepository endpointRepository;
    private final DeliveryService deliveryService;

    public DeliveryConsumer(EventRepository eventRepository,
                            EndpointRepository endpointRepository,
                            DeliveryService deliveryService) {
        this.eventRepository = eventRepository;
        this.endpointRepository = endpointRepository;
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = KafkaTopicConfig.EVENTS_TOPIC)
    @Transactional
    public void onEvent(String eventId) {
        log.info("Kafka message received for event {}", eventId);

        Event event = eventRepository.findById(UUID.fromString(eventId)).orElse(null);
        if (event == null) {
            log.warn("Event {} not found, skipping", eventId);
            return;
        }

        List<Endpoint> endpoints = endpointRepository.findByTenantId(event.getTenantId());
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getSubscribedEventTypes() != null
                    && endpoint.getSubscribedEventTypes().contains(event.getEventType())) {
                deliveryService.attemptDelivery(event, endpoint, 1);
            }
        }
    }
}