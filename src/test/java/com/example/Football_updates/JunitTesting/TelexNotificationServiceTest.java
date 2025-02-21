package com.example.Football_updates.JunitTesting;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.Football_updates.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TelexNotificationServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelexNotificationService telexNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendFootballUpdate_Success() {
        String match = "Team A vs Team B";
        String message = "Goal Scored";

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        StepVerifier.create(telexNotificationService.sendFootballUpdate(match, message))
                .verifyComplete();
    }
}
