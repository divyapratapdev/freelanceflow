package com.freelanceflow.invoice.dto;

import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceLineItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class InvoiceResponse {

    private Long id;
    private Long userId;
    private Long clientId;
    private Long projectId;
    private Long invoiceNumber;
    private InvoiceStatus status;
    private List<InvoiceLineItem> lineItems;
    private BigDecimal subtotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private LocalDate dueDate;
    private String notes;
    private String razorpayPaymentLinkId;
    private String razorpayPaymentLinkUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public InvoiceResponse() {}

    public static InvoiceResponse from(Invoice inv) {
        InvoiceResponse r = new InvoiceResponse();
        r.id = inv.getId();
        r.userId = inv.getUserId();
        r.clientId = inv.getClientId();
        r.projectId = inv.getProjectId();
        r.invoiceNumber = inv.getInvoiceNumber();
        r.status = inv.getStatus();
        r.lineItems = inv.getLineItems();
        r.subtotal = inv.getSubtotal();
        r.taxPercent = inv.getTaxPercent();
        r.taxAmount = inv.getTaxAmount();
        r.total = inv.getTotal();
        r.dueDate = inv.getDueDate();
        r.notes = inv.getNotes();
        r.razorpayPaymentLinkId = inv.getRazorpayPaymentLinkId();
        r.razorpayPaymentLinkUrl = inv.getRazorpayPaymentLinkUrl();
        r.createdAt = inv.getCreatedAt();
        r.updatedAt = inv.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getClientId() { return clientId; }
    public Long getProjectId() { return projectId; }
    public Long getInvoiceNumber() { return invoiceNumber; }
    public InvoiceStatus getStatus() { return status; }
    public List<InvoiceLineItem> getLineItems() { return lineItems; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotal() { return total; }
    public LocalDate getDueDate() { return dueDate; }
    public String getNotes() { return notes; }
    public String getRazorpayPaymentLinkId() { return razorpayPaymentLinkId; }
    public String getRazorpayPaymentLinkUrl() { return razorpayPaymentLinkUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
