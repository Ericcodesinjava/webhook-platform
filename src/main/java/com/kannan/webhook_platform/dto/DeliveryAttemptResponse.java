package com.kannan.webhook_platform.dto;

import com.kannan.webhook_platform.entity.DeliveryAttempt;
import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryAttemptResponse {
    private UUID id;
    private UUID endpointId;
    private int attemptNumber;
    private String status;
    private Integer responseStatusCode;
    private String errorMessage;
    private LocalDateTime attemptedAt;

    public static DeliveryAttemptResponse fromEntity(DeliveryAttempt a) {
        DeliveryAttemptResponse r = new DeliveryAttemptResponse();
        r.id = a.getId();
        r.endpointId = a.getEndpointId();
        r.attemptNumber = a.getAttemptNumber();
        r.status = a.getStatus();
        r.responseStatusCode = a.getResponseStatusCode();
        r.errorMessage = a.getErrorMessage();
        r.attemptedAt = a.getAttemptedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getEndpointId() { return endpointId; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getStatus() { return status; }
    public Integer getResponseStatusCode() { return responseStatusCode; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
}