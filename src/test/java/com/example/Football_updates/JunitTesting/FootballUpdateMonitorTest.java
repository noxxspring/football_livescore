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

public class FootballUpdateMonitorTest {
    @Mock
    private FootballUpdateService footballUpdateService;

    @Mock
    private TelexNotificationService telexNotificationService;

    @InjectMocks
    private FootballUpdateMonitor footballUpdateMonitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchAndSendLiveScores() {
        List<Map<String, Object>> fakeMatches = new ArrayList<>();
        Map<String, Object> match = new HashMap<>();
        match.put("homeTeam", "Team A");
        match.put("awayTeam", "Team B");
        match.put("score", "2-1");
        fakeMatches.add(match);

        when(footballUpdateService.getFootballSummary()).thenReturn(Mono.just(fakeMatches));
        when(telexNotificationService.sendFootballUpdate(anyString(), anyString())).thenReturn(Mono.empty());

        footballUpdateMonitor.fetchAndSendLiveScores();
        verify(telexNotificationService, times(1)).sendFootballUpdate(anyString(), anyString());
    }
}

