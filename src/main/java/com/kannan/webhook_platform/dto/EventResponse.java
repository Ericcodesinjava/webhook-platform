package com.kannan.webhook_platform.dto;

import com.kannan.webhook_platform.entity.Event;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventResponse {
    private UUID id;
    private String eventType;
    private LocalDateTime createdAt;

    public static EventResponse fromEntity(Event event) {
        EventResponse r = new EventResponse();
        r.id = event.getId();
        r.eventType = event.getEventType();
        r.createdAt = event.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}