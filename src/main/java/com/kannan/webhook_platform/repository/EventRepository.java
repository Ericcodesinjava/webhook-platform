package com.kannan.webhook_platform.repository;

import com.kannan.webhook_platform.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}