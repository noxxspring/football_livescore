package com.example.Football_updates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class TelexNotificationService {
    private final String telexWebhookUrl = "https://ping.telex.im/v1/webhooks/01952b33-de08-710c-bf73-b359d4cf2cf2";
    private final RestTemplate restTemplate;

    public TelexNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Method to send notification to Telex with a formatted payload
    public Mono<Void> sendFootballUpdate(String match, String message) {
        // Format the payload
        String payload = String.format(
                "{\"message\":\"%s: %s\",\"username\":\"Football Update\",\"event_name\":\"Football Match Update\",\"status\":\"%s\"}",
                match, message, message.equals(".*\\d+.*") ? "error" : "success");

        // Prepare the headers for the POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the payload into an HttpEntity
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            // Send POST request to Telex webhook (no response body is expected)
            restTemplate.exchange(telexWebhookUrl, HttpMethod.POST, request, Void.class);

            return Mono.empty();  // Return an empty Mono to indicate success without a response

        } catch (Exception e) {
            // Handle error and return a Mono error in case of an exception
            System.err.println("Error sending to Telex: " + e.getMessage());
            return Mono.error(e);  // Return Mono error if something goes wrong
        }
    }
}