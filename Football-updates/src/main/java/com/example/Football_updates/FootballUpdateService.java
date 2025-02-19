
package com.example.Football_updates;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FootballUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(FootballUpdateService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20000);


    private static final String API_KEY = "MLk30EfD29QBQ8qvz6MZdAuBoGLgBHAnqEfayM2b";
    private static final String API_URL = "https://api.sportradar.com/soccer/trial/v4/en/schedules/live/schedules.json";

//    @Value("${football.api.key}")
//    private String apiKey;
//
//    @Value("${football.api.url}")
//    private String apiUrl; // Ensure this includes the protocol (e.g., https://api.sportradar.com)

    private final WebClient webClient;

    public FootballUpdateService(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(REQUEST_TIMEOUT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .resolver(spec -> spec.queryTimeout(Duration.ofSeconds(10))) // Increase DNS timeout
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30)) // Add a read timeout handler
                        .addHandlerLast(new WriteTimeoutHandler(30))); // Add a write timeout handler

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://api.sportradar.com") // Base URL should include the protocol and host (e.g., https://api.sportradar.com)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    public Mono<List<Map<String, Object>>> getFootballSummary() {
        String endpointPath = API_URL + "?api_key=" + API_KEY;
        return webClient.get()
                .uri(endpointPath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error("Error response: {}", body);
                            return Mono.error(new WebClientResponseException(
                                    "Error occurred while fetching data from API. Status: " + response.statusCode(),
                                    response.statusCode().value(),
                                    response.statusCode().toString(),
                                    response.headers().asHttpHeaders(),
                                    body.getBytes(),
                                    null
                            ));
                        }))
                .bodyToMono(String.class) // Convert response body to String
                .timeout(REQUEST_TIMEOUT)
                .retry(3) // Retry up to 3 times
                .doOnNext(responseBody -> {
                    logger.info("Raw API Response: {}", responseBody);
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                        logger.info("Logged responseMap: {}", responseMap);
                    } catch (JsonProcessingException e) {
                        logger.error("Error logging JSON response", e);
                    }
                })
                .flatMap(this::parseJsonResponse) // Parse JSON response
                .onErrorResume(e -> {
                    logger.error("Error while fetching football summary", e);
                    return Mono.just(List.of()); // Return empty list on error
                });
    }

    private Mono<List<Map<String, Object>>> parseJsonResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            logger.error("Empty response body received.");
            return Mono.just(List.of());
        }

// Check if the response is HTML (starts with '<')
        if (responseBody.trim().startsWith("<")) {
            logger.error("Received HTML response instead of JSON: {}", responseBody);
            return Mono.just(List.of());
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            logger.info("parsed response: {}", responseMap);

            if (responseMap.containsKey("schedules")) {
                List<Map<String, Object>> schedules = (List<Map<String, Object>>) responseMap.get("schedules");

// Extract relevant match details
                List<Map<String, Object>> matchDetails = new ArrayList<>();
                for (Map<String, Object> schedule : schedules) {
                    Map<String, Object> sportEvent = (Map<String, Object>) schedule.get("sport_event");
                    Map<String, Object> sportEventStatus = (Map<String, Object>) schedule.get("sport_event_status");


                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("homeTeam", extractTeamName(sportEvent, "home"));
                    matchData.put("awayTeam", extractTeamName(sportEvent, "away"));
                    matchData.put("score", extractScore(sportEventStatus));
                    matchDetails.add(matchData);
                }
                return Mono.just(matchDetails);
            } else {
                logger.error("No 'schedules' key found in response: {}", responseMap);
                return Mono.just(List.of());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON response", e);
            return Mono.just(List.of());
        }
    }

    private String extractTeamName(Map<String, Object> sportEvent, String teamQualifier) {
        try {
            List<Map<String, Object>> competitors = (List<Map<String, Object>>) sportEvent.get("competitors");

            for (Map<String, Object> competitor : competitors) {
                if (competitor.get("qualifier").equals(teamQualifier)) {
                    Object teamName = competitor.get("name");

// Debugging: Log the type of teamName
                    logger.info("Type of teamName for {}: {}", teamQualifier, teamName != null ? teamName.getClass().getName() : "null");

// If it's a String, use it directly
                    if (teamName instanceof String) {
                        return (String) teamName;
                    }
// If it's a Map, extract the "name" field from it
                    else if (teamName instanceof Map) {
// If 'teamName' is a Map, extract the "name" from it
                        Object name = ((Map<?, ?>) teamName).get("name");
                        if (name instanceof String) {
                            return (String) name;
                        } else {
                            return "Unknown Team";
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting team name for qualifier: {}", teamQualifier, e);
        }
        return "Unknown Team";
    }

    private String extractScore(Map<String, Object> sportEventStatus) {
        try {
// Log the sportEventStatus for debugging
            logger.info("sportEventStatus: {}", sportEventStatus);

// Check for the existence of the keys
            if (!sportEventStatus.containsKey("home_score") || !sportEventStatus.containsKey("away_score")) {
                logger.error("Missing score keys in sportEventStatus: {}", sportEventStatus);
                return "N/A";
            }

            Object homeScore = sportEventStatus.get("home_score");
            Object awayScore = sportEventStatus.get("away_score");

// Debugging: Log the types of homeScore and awayScore
            logger.info("Type of homeScore: {}, Type of awayScore: {}",
                    homeScore != null ? homeScore.getClass().getName() : "null",
                    awayScore != null ? awayScore.getClass().getName() : "null");

// Ensure homeScore and awayScore are Strings (if they're Integers)
            if (homeScore instanceof Integer) {
                homeScore = homeScore.toString();
            } else if (!(homeScore instanceof String)) {
                homeScore = "0"; // Fallback value
            }

            if (awayScore instanceof Integer) {
                awayScore = awayScore.toString();
            } else if (!(awayScore instanceof String)) {
                awayScore = "0"; // Fallback value
            }

            return String.format("%s - %s", homeScore, awayScore);
        } catch (Exception e) {
            logger.error("Error extracting score", e);
        }
        return "N/A";
    }
}