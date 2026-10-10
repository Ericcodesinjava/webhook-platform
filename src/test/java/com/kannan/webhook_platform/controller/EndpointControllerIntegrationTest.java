package com.kannan.webhook_platform.controller;

import com.kannan.webhook_platform.entity.ApiKey;
import com.kannan.webhook_platform.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EndpointControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private final String testApiKey = "test-key-123";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        ApiKey key = new ApiKey();
        key.setKeyValue(testApiKey);
        key.setTenantId("t1");
        apiKeyRepository.save(key);
    }

    @Test
    void createEndpoint_withValidApiKey_returns200() throws Exception {
        String body = """
            {"tenantId":"t1","url":"https://example.com","subscribedEventTypes":["payment.succeeded"]}
            """;

        mockMvc.perform(post("/endpoints")
                        .header("X-API-Key", testApiKey)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void createEndpoint_withoutApiKey_returns403() throws Exception {
        String body = """
            {"tenantId":"t1","url":"https://example.com","subscribedEventTypes":["payment.succeeded"]}
            """;

        mockMvc.perform(post("/endpoints")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }
}