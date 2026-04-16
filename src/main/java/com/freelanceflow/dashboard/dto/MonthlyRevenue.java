package com.freelanceflow.dashboard.dto;

import java.math.BigDecimal;

public class MonthlyRevenue {
    private String month;
    private BigDecimal amount;

    public MonthlyRevenue() {}

    public MonthlyRevenue(String month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
