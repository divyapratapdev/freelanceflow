package com.freelanceflow.ai;

import com.freelanceflow.config.GrokAiConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real implementation of AI Client that connects to the Grok/Groq API.
 */
@Component
@Profile("!demo")
public class RealGrokApiClient implements AiClient {

    private final GrokAiConfig grokAiConfig;
    private final RestTemplate restTemplate;

    public RealGrokApiClient(GrokAiConfig grokAiConfig, RestTemplate restTemplate) {
        this.grokAiConfig = grokAiConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getCompletion(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(grokAiConfig.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", grokAiConfig.getModel());
        
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    grokAiConfig.getBaseUrl() + "/chat/completions",
                    request,
                    Map.class
            );

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Grok API", e);
        }
    }
}
