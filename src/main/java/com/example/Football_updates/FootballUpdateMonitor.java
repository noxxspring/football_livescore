package com.example.Football_updates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class FootballUpdateMonitor {

    private static final Logger logger = LoggerFactory.getLogger(FootballUpdateMonitor.class);

    private final FootballUpdateService footballUpdateService;
    private final TelexNotificationService telexNotificationService;

    public FootballUpdateMonitor(FootballUpdateService footballUpdateService, TelexNotificationService telexNotificationService) {
        this.footballUpdateService = footballUpdateService;
        this.telexNotificationService = telexNotificationService;
    }

    @Scheduled(cron = "0 0 * * * *")  // Run at the start of every hour (cron expression for hourly schedule)
    public void fetchAndSendLiveScores() {
        footballUpdateService.getFootballSummary()
                .flatMapMany(Flux::fromIterable)  // Convert List to Flux (Reactive Stream)
                .flatMap(match -> {
                    // Extract match details
                    String homeTeam = getTeamName(match, "homeTeam");
                    String awayTeam = getTeamName(match, "awayTeam");
                    String score = getScore(match);

                    String matchMessage = String.format("Match: %s vs %s:", homeTeam, awayTeam);
                    logger.info("Sending to Telex: {}", matchMessage);

                    return telexNotificationService.sendFootballUpdate(matchMessage, score);
                })
                .doOnComplete(() -> logger.info("All live scores sent to Telex successfully."))
                .subscribe(); // Trigger the reactive pipeline
    }

    // Helper method to safely extract team name
    private String getTeamName(Map<String, Object> match, String teamKey) {
        try {
            Object teamObject = match.get(teamKey);

            // Check if the team data is a Map or a String
            if (teamObject instanceof Map) {
                Map<String, Object> teamMap = (Map<String, Object>) teamObject;
                return (String) teamMap.getOrDefault("name", "Unknown Team");
            } else if (teamObject instanceof String) {
                return (String) teamObject;  // If it's a String, return it directly
            } else {
                return "Unknown Team";  // Default if it's neither a Map nor a String
            }
        } catch (Exception e) {
            logger.error("Error extracting team name for key: " + teamKey, e);
            return "Unknown Team";  // Return default value in case of error
        }
    }

    // Helper method to safely extract score
    private String getScore(Map<String, Object> match) {
        try {
            Object scoreObject = match.get("score");

            // Check if score is a Map or a String
            if (scoreObject instanceof Map) {
                Map<String, Object> scoreMap = (Map<String, Object>) scoreObject;
                return (String) scoreMap.getOrDefault("fulltime", "N/A");
            } else if (scoreObject instanceof String) {
                return (String) scoreObject;  // If score is a String, return it directly
            } else {
                return "N/A";  // Default if it's neither a Map nor a String
            }
        } catch (Exception e) {
            logger.error("Error extracting score", e);
            return "N/A";  // Return default value in case of error
        }
    }}