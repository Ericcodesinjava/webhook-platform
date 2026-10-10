package com.kannan.webhook_platform.dto;

import com.kannan.webhook_platform.entity.Endpoint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EndpointResponse {
    // Rep what our api sends back to the client.
    private UUID id;
    private String url;
    private List<String> subscribedEventTypes;
    private LocalDateTime createdAt;

    // Notice: no "secret" field here at all — that's the whole point.

    public static EndpointResponse fromEntity(Endpoint endpoint) {
        EndpointResponse response = new EndpointResponse();
        response.id = endpoint.getId();
        response.url = endpoint.getUrl();
        response.subscribedEventTypes = endpoint.getSubscribedEventTypes();
        response.createdAt = endpoint.getCreatedAt();
        return response;
    }

    public UUID getId() { return id; }
    public String getUrl() { return url; }
    public List<String> getSubscribedEventTypes() { return subscribedEventTypes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}