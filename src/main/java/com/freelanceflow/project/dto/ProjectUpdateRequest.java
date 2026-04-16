package com.freelanceflow.project.dto;

import com.freelanceflow.common.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectUpdateRequest {

    private String name;
    private String description;
    private ProjectStatus status;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;

    public ProjectUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
