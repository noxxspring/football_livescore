package com.example.Football_updates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@CrossOrigin("*")
public class FootballUpdateController {

    @Value("${telex.integration.app_name}")
    private String appName;

    @Value("${telex.integration.settings.interval}")
    private String interval;

    @Value("${telex.integration.app_description}")
    private String appDescription;

    @Value("${telex.integration.app_logo}")
    private String appLogo;

    @Value("${telex.integration.app_url}")
    private String appUrl;

    @Value("${telex.integration.background_color}")
    private String backgroundColor;

    @Value("${telex.integration.is_active}")
    private boolean isActive;

    @Value("${telex.integration.integration_type}")
    private String integrationType;

    @Value("${telex.integration.key_features}")
    private List<String> keyFeatures;

    @Value("${telex.integration.author}")
    private String author;

    @Value("${telex.integration.settings.time_interval}")
    private String timeInterval;

    @Value("${telex.integration.integration_category}")
    private String integration_category;

    @Value("${telex.integration.settings.event_type}")
    private String eventType;

    @Value("${telex.integration.target_url}")
    private String targetUrl;

    @Value("${telex.integration.tick_url}")
    private String tickUrl;

    private final FootballUpdateMonitor footballUpdateMonitor;
    private static final Logger logger = LoggerFactory.getLogger(FootballUpdateController.class);


    public FootballUpdateController(FootballUpdateMonitor footballUpdateMonitor){
        this.footballUpdateMonitor = footballUpdateMonitor;
    }

    // endpoint that returns the integration.json
    @GetMapping(value = "/integration.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getIntegrationJson () {
        // Use LinkedHashMap to maintain insertion order
        Map<String, Object> descriptions = new LinkedHashMap<>();
        descriptions.put("app_name", appName);
        descriptions.put("app_description", appDescription);
        descriptions.put("app_url", appUrl);
        descriptions.put("app_logo", appLogo);
        descriptions.put("background_color", backgroundColor);


        List<Map<String, Object>> settings = new ArrayList<>();

        Map<String, Object> timeIntervalSetting = new LinkedHashMap<>();
        timeIntervalSetting.put("label", "time_interval");
        timeIntervalSetting.put("type", "dropdown");
        timeIntervalSetting.put("required", true);
        timeIntervalSetting.put("default", "one-hour");

        Map<String, Object> eventTypeSetting = new LinkedHashMap<>();
        eventTypeSetting.put("label", "event_type");
        eventTypeSetting.put("type", "text");
        eventTypeSetting.put("required", true);
        eventTypeSetting.put("default", "* * * * *"); // Ensure eventType is correctly set

        Map<String, Object> intervalSetting = new LinkedHashMap<>();
        intervalSetting.put("label","interval");
        intervalSetting.put("type","text");
        intervalSetting.put("required",true);
        intervalSetting.put("default", "* * * * *");

        settings.add(timeIntervalSetting);
        settings.add(eventTypeSetting);
        settings.add(intervalSetting);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("descriptions", descriptions);
        data.put("integration_type",integrationType);
        data.put("is_active",false);
        data.put("integration_category", integration_category);
        data.put("key_features",keyFeatures);
        data.put("settings", settings);
        data.put("target_url","");
        data.put("tick_url", tickUrl); // Ensures correct URL

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", data);

        System.out.println("Returning JSON: " + response); // Debugging

        return response;
    }

    // tick endpoint, that will post to the telex Api
    @PostMapping("/tick")
    public Mono<ResponseEntity<String>> handleFootballUpdate(@RequestBody MonitorPayload payload) {
        logger.info("Received tick request with payload: {}", payload);

        return Mono.fromRunnable(() -> footballUpdateMonitor.fetchAndSendLiveScores())
                .thenReturn(ResponseEntity.ok("Football Live scores sent to Telex"))
                .onErrorResume(e -> {
                    logger.error("Error while fetching and sending football live scores", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to fetch and send football live scores"));
                });
    }}