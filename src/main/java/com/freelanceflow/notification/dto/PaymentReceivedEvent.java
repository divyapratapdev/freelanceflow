package com.freelanceflow.notification.dto;

import java.math.BigDecimal;

public class PaymentReceivedEvent {
    private Long invoiceId;
    private Long userId;
    private Long clientId;
    private BigDecimal amount;

    public PaymentReceivedEvent() {}

    public PaymentReceivedEvent(Long invoiceId, Long userId, Long clientId, BigDecimal amount) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.clientId = clientId;
        this.amount = amount;
    }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
