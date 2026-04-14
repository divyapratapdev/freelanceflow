package com.freelanceflow.ai.dto;

import com.freelanceflow.ai.AiInsight;
import com.freelanceflow.common.enums.InsightType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class AiInsightResponse {

    private Long id;
    private InsightType insightType;
    private String title;
    private String description;
    private Long affectedClientId;
    private BigDecimal predictedAmount;
    private LocalDate validUntil;
    private Instant createdAt;

    public AiInsightResponse() {}

    public static AiInsightResponse from(AiInsight i) {
        AiInsightResponse r = new AiInsightResponse();
        r.id = i.getId();
        r.insightType = i.getInsightType();
        r.title = i.getTitle();
        r.description = i.getDescription();
        r.affectedClientId = i.getAffectedClientId();
        r.predictedAmount = i.getPredictedAmount();
        r.validUntil = i.getValidUntil();
        r.createdAt = i.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public InsightType getInsightType() { return insightType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getAffectedClientId() { return affectedClientId; }
    public BigDecimal getPredictedAmount() { return predictedAmount; }
    public LocalDate getValidUntil() { return validUntil; }
    public Instant getCreatedAt() { return createdAt; }
}
