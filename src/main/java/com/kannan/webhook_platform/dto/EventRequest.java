package com.kannan.webhook_platform.dto;
import jakarta.validation.constraints.NotBlank;
public class EventRequest {
    @NotBlank(message = "tenantId is required")
    private String tenantId;
    @NotBlank(message = "eventType is required")
    private String eventType;
    @NotBlank(message = "payload is required")
    private String payload;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}