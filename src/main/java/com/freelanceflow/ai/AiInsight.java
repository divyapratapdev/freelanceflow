package com.freelanceflow.ai;

import com.freelanceflow.common.enums.InsightType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "ai_insights")
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false, length = 50)
    private InsightType insightType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "affected_client_id")
    private Long affectedClientId;

    @Column(name = "predicted_amount", precision = 10, scale = 2)
    private BigDecimal predictedAmount;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AiInsight() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public InsightType getInsightType() { return insightType; }
    public void setInsightType(InsightType insightType) { this.insightType = insightType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAffectedClientId() { return affectedClientId; }
    public void setAffectedClientId(Long affectedClientId) { this.affectedClientId = affectedClientId; }

    public BigDecimal getPredictedAmount() { return predictedAmount; }
    public void setPredictedAmount(BigDecimal predictedAmount) { this.predictedAmount = predictedAmount; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public Instant getCreatedAt() { return createdAt; }
}
