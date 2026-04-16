package com.freelanceflow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private BigDecimal totalRevenue;
    private BigDecimal outstandingAmount;
    private BigDecimal overdueAmount;
    private long activeProjects;
    private long totalClients;
    private List<MonthlyRevenue> revenueChart; // Last 6 months

    public DashboardResponse() {}

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }

    public BigDecimal getOverdueAmount() { return overdueAmount; }
    public void setOverdueAmount(BigDecimal overdueAmount) { this.overdueAmount = overdueAmount; }

    public long getActiveProjects() { return activeProjects; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }

    public long getTotalClients() { return totalClients; }
    public void setTotalClients(long totalClients) { this.totalClients = totalClients; }

    public List<MonthlyRevenue> getRevenueChart() { return revenueChart; }
    public void setRevenueChart(List<MonthlyRevenue> revenueChart) { this.revenueChart = revenueChart; }
}
