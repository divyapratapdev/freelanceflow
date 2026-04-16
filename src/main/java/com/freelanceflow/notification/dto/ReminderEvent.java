package com.freelanceflow.notification.dto;

import com.freelanceflow.common.enums.ReminderType;

public class ReminderEvent {
    private Long invoiceId;
    private Long userId;
    private Long clientId;
    private ReminderType reminderType;

    public ReminderEvent() {}

    public ReminderEvent(Long invoiceId, Long userId, Long clientId, ReminderType reminderType) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.clientId = clientId;
        this.reminderType = reminderType;
    }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }
}
