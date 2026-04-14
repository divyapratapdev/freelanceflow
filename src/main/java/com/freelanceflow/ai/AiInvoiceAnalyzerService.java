package com.freelanceflow.ai;

import com.freelanceflow.common.enums.InsightType;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AiInvoiceAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(AiInvoiceAnalyzerService.class);

    private final InvoiceRepository invoiceRepository;
    private final AiInsightRepository aiInsightRepository;
    private final AiClient aiClient;

    public AiInvoiceAnalyzerService(InvoiceRepository invoiceRepository,
                                    AiInsightRepository aiInsightRepository,
                                    AiClient aiClient) {
        this.invoiceRepository = invoiceRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.aiClient = aiClient;
    }

    @Async
    public void analyzeInvoicesForUser(Long userId) {
        log.info("Starting AI invoice analysis for user {}", userId);

        // Simple aggregation for analysis context
        List<Invoice> recentInvoices = invoiceRepository.findByUserId(userId, Pageable.ofSize(50)).getContent();
        
        long overdueCount = recentInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.OVERDUE).count();
        long totalInvoices = recentInvoices.size();

        String systemPrompt = "You are a business AI analyst for Indian freelancers. Analyze the text data and propose highly actionable insights. " +
                "Respond ONLY in exact JSON format matching this schema: " +
                "{\"title\":\"...\", \"description\":\"...\", \"type\":\"REVENUE_FORECAST|OVERDUE_RISK|PROFITABLE_CLIENT\"}";

        String userMessage = String.format("Analyze this: User has %d total recent invoices. %d are currently overdue.", totalInvoices, overdueCount);

        try {
            String responseStr = aiClient.getCompletion(systemPrompt, userMessage);
            if (responseStr != null) {
                // Strip markdown fences before parsing
                String jsonBody = responseStr.replaceAll("(?i)```[a-zA-Z]*\\s*\\n?", "").replaceAll("```\\s*", "").trim();
                JSONObject json = new JSONObject(jsonBody);

                AiInsight insight = new AiInsight();
                insight.setUserId(userId);
                insight.setTitle(json.getString("title"));
                insight.setDescription(json.getString("description"));
                insight.setInsightType(InsightType.valueOf(json.optString("type", "REVENUE_FORECAST")));
                insight.setValidUntil(LocalDate.now().plusDays(7));
                aiInsightRepository.save(insight);
                log.info("Successfully saved AI insight for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to analyze invoices for user {}", userId, e);
        }
    }

    public Page<AiInsight> getInsights(Long userId, Pageable pageable) {
        return aiInsightRepository.findByUserId(userId, pageable);
    }
}
