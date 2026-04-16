package com.freelanceflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrokAiConfig {

    @Value("${grok.api.key}")
    private String apiKey;

    @Value("${grok.api.base-url}")
    private String baseUrl;

    @Value("${grok.api.model}")
    private String model;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }
}
