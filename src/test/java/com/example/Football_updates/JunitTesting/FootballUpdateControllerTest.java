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

import java.util.*;

public class FootballUpdateControllerTest {

    @Mock
    private FootballUpdateMonitor footballUpdateMonitor;

    @InjectMocks
    private FootballUpdateController footballUpdateController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetIntegrationJson() {
        Map<String, Object> response = footballUpdateController.getIntegrationJson();
        assertNotNull(response);
        assertTrue(response.containsKey("data"));
    }
}
