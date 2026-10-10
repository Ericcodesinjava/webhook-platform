package com.kannan.webhook_platform.config;

import com.kannan.webhook_platform.entity.ApiKey;
import com.kannan.webhook_platform.repository.ApiKeyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DevApiKeySeeder implements CommandLineRunner {

    private final ApiKeyRepository apiKeyRepository;

    public DevApiKeySeeder(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void run(String... args) {
        String testKey = UUID.randomUUID().toString();

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(testKey);
        apiKey.setTenantId("t1");
        apiKeyRepository.save(apiKey);

        System.out.println("=====================================");
        System.out.println("DEV API KEY for tenant t1: " + testKey);
        System.out.println("=====================================");
    }
}