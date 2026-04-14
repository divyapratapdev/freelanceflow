package com.freelanceflow.aiquery;

import com.freelanceflow.ai.AiClient;
import com.freelanceflow.aiquery.tools.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiQueryService {

    // Simple manual tool routing for Grok (which might not natively support OpenAI tool definitions perfectly)
    // We fetch context based on basic keyword matching since full Spring AI tool calling is best with OpenAI/Anthropic
    // For production with Grok, we manually inject common context to answer natural language queries.
    
    private final AiClient aiClient;
    private final GetDashboardStatsTool dashboardStatsTool;
    private final GetInvoicesTool invoicesTool;

    public AiQueryService(AiClient aiClient,
                          GetDashboardStatsTool dashboardStatsTool,
                          GetInvoicesTool invoicesTool) {
        this.aiClient = aiClient;
        this.dashboardStatsTool = dashboardStatsTool;
        this.invoicesTool = invoicesTool;
    }

    public String executeQuery(Long userId, String query) {
        // Collect context
        JSONObject context = new JSONObject();
        try {
            context.put("dashboard_stats", new JSONObject(dashboardStatsTool.apply(userId)));
            
            // Limit to 20 invoices max to avoid token limits
            List<?> invoices = invoicesTool.apply(new GetInvoicesTool.Request(userId, null));
            int limit = Math.min(invoices.size(), 20);
            context.put("recent_invoices", new JSONArray(invoices.subList(0, limit)));
        } catch (Exception e) {
            // Context collection failed, continue with whatever we have
        }

        String systemPrompt = "You are a helpful business assistant for an Indian freelancer. " +
                "You have access to the user's dashboard stats and recent invoices as JSON context: " + context.toString() +
                ". \nAnswer the user's query clearly and concisely based ONLY on this context. " +
                "Don't expose raw JSON. Don't say 'based on the context'. Just answer directly.";

        return aiClient.getCompletion(systemPrompt, query);
    }
}
