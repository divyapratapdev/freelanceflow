package com.freelanceflow.notification.dto;

public class InvoiceSentEvent {
    private Long invoiceId;
    private Long userId;
    private Long clientId;

    public InvoiceSentEvent() {}

    public InvoiceSentEvent(Long invoiceId, Long userId, Long clientId) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.clientId = clientId;
    }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}
