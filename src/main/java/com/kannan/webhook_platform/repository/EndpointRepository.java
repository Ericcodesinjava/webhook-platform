package com.kannan.webhook_platform.repository;

import com.kannan.webhook_platform.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    List<Endpoint> findByTenantId(String tenantId);

}