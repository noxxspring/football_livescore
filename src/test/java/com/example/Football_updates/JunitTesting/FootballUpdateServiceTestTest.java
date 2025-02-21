package com.example.Football_updates.JunitTesting;

import com.example.Football_updates.FootballUpdateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class FootballUpdateServiceTestTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private FootballUpdateService footballUpdateService;

    @BeforeEach
    public void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void testGetFootballSummary_success() {
        // Mocking WebClient to return a mock response
        String mockResponse = "{ \"schedules\": [] }";
        when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just(mockResponse));

        Mono<List<Map<String, Object>>> result = footballUpdateService.getFootballSummary();

        StepVerifier.create(result)
                .expectNextMatches(matches -> matches.isEmpty())
                .verifyComplete();
    }

    @Test
    public void testGetFootballSummary_failure() {
        when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        Mono<List<Map<String, Object>>> result = footballUpdateService.getFootballSummary();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}