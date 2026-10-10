package com.kannan.webhook_platform.service;

import com.kannan.webhook_platform.entity.Endpoint;
import com.kannan.webhook_platform.repository.EndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class EndpointServiceTest {

    @Mock
    private EndpointRepository endpointRepository;

    @InjectMocks
    private EndpointService endpointService;

    @Test
    void createEndpoint_savesAndReturnsEndpointWithGeneratedSecret() {
        when(endpointRepository.save(org.mockito.ArgumentMatchers.any(Endpoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Endpoint result = endpointService.createEndpoint("t1", "https://example.com", List.of("payment.succeeded"));

        assertThat(result.getTenantId()).isEqualTo("t1");
        assertThat(result.getUrl()).isEqualTo("https://example.com");
        assertThat(result.getSecret()).isNotBlank();
    }
}