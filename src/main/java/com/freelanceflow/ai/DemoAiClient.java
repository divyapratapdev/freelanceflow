package com.freelanceflow.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Expert-level Demo Implementation.
 * Returns a high-quality, pre-written business insight for high-impact video demos.
 * This ensures the demo is "God Level" regardless of API connectivity.
 */
@Component
@Profile("demo")
public class DemoAiClient implements AiClient {

    @Override
    public String getCompletion(String systemPrompt, String userMessage) {
        // We simulate a slight delay to make it look "human" and "computed" in the video
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        return "{\n" +
                "  \"title\": \"Revenue Optimization Opportunity\",\n" +
                "  \"description\": \"Based on your current billing cycle and client payment patterns, increasing your 'Cloud Infrastructure' unit price by 12% matches industry standards for expert-level consultants in 2026. This would result in an estimated ₹9,000 increase in monthly recurring revenue (MRR).\",\n" +
                "  \"type\": \"REVENUE_FORECAST\"\n" +
                "}";
    }
}
