package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.repository.EndpointRepository;
import org.springframework.stereotype.Service;
import com.kannan.webhook_platform.exception.EndpointNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;

    // Constructor injection: Spring sees this constructor,
    // notices it needs an EndpointRepository, and automatically
    // passes in the bean it already created for that interface.
    // We never call "new EndpointService(...)" ourselves.
    public EndpointService(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    public Endpoint createEndpoint(String tenantId, String url, List<String> subscribedEventTypes) {
        Endpoint endpoint = new Endpoint();
        endpoint.setTenantId(tenantId);
        endpoint.setUrl(url);
        endpoint.setSubscribedEventTypes(subscribedEventTypes);
        endpoint.setSecret(generateSecret());
        return endpointRepository.save(endpoint);
    }

    public List<Endpoint> getEndpointsForTenant(String tenantId) {
        return endpointRepository.findByTenantId(tenantId);
    }

    public Endpoint updateEndpoint(UUID id, String url, List<String> subscribedEventTypes) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new EndpointNotFoundException("Endpoint not found: " + id));
        if (url != null) endpoint.setUrl(url);
        if (subscribedEventTypes != null) endpoint.setSubscribedEventTypes(subscribedEventTypes);
        return endpointRepository.save(endpoint);
    }

    public void deleteEndpoint(UUID id) {
        endpointRepository.deleteById(id);
    }

    private String generateSecret() {
        return UUID.randomUUID().toString();
    }
}