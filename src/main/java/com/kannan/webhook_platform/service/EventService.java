package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.DeliveryAttempt;
import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.entity.Event;
import com.kannan.webhook_platform.repository.DeliveryAttemptRepository;
import com.kannan.webhook_platform.repository.EndpointRepository;
import com.kannan.webhook_platform.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EndpointRepository endpointRepository;
    private final DeliveryService deliveryService;
    private final DeliveryAttemptRepository deliveryAttemptRepository;

    public EventService(EventRepository eventRepository,
                        EndpointRepository endpointRepository,
                        DeliveryService deliveryService,
                        DeliveryAttemptRepository deliveryAttemptRepository) {
        this.eventRepository = eventRepository;
        this.endpointRepository = endpointRepository;
        this.deliveryService = deliveryService;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
    }

    public Event ingestEvent(String tenantId, String eventType, String payload) {
        Event event = new Event();
        event.setTenantId(tenantId);
        event.setEventType(eventType);
        event.setPayload(payload);
        Event saved = eventRepository.save(event);

        List<Endpoint> endpoints = endpointRepository.findByTenantId(tenantId);
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getSubscribedEventTypes() != null
                    && endpoint.getSubscribedEventTypes().contains(eventType)) {
                deliveryService.attemptDelivery(saved, endpoint, 1);
            }
        }

        return saved;
    }

    public List<DeliveryAttempt> getDeliveryHistory(UUID eventId) {
        return deliveryAttemptRepository.findByEventIdOrderByAttemptedAtAsc(eventId);
    }
}