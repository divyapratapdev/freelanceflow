package com.freelanceflow.project.dto;

import com.freelanceflow.common.enums.ProjectStatus;
import com.freelanceflow.project.Project;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ProjectResponse {

    private Long id;
    private Long userId;
    private Long clientId;
    private String name;
    private String description;
    private ProjectStatus status;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectResponse() {}

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.userId = p.getUserId();
        r.clientId = p.getClientId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.status = p.getStatus();
        r.budget = p.getBudget();
        r.startDate = p.getStartDate();
        r.endDate = p.getEndDate();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getClientId() { return clientId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public BigDecimal getBudget() { return budget; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
