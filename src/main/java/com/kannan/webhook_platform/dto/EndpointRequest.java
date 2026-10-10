package com.kannan.webhook_platform.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class EndpointRequest {
    // rep what the client is allowed to send to our api
    @NotBlank(message = "tenantId is required")
    private String tenantId;
    @NotBlank(message = "url is required")
    private String url;
    @NotEmpty(message = "subscribedEventTypes must have at least one entry")
    private List<String> subscribedEventTypes;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<String> getSubscribedEventTypes() { return subscribedEventTypes; }
    public void setSubscribedEventTypes(List<String> subscribedEventTypes) { this.subscribedEventTypes = subscribedEventTypes; }
}