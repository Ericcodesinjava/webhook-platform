package com.kannan.webhook_platform.config;   // must match SecurityConfig's package line

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String EVENTS_TOPIC = "webhook.events";

    @Bean
    public NewTopic webhookEventsTopic() {
        return TopicBuilder.name(EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}