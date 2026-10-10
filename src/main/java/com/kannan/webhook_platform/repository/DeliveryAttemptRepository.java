package com.kannan.webhook_platform.repository;

import com.kannan.webhook_platform.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
    List<DeliveryAttempt> findByStatusAndNextRetryAtLessThanEqual(String status, LocalDateTime time);
    List<DeliveryAttempt> findByEventIdOrderByAttemptedAtAsc(java.util.UUID eventId);
}