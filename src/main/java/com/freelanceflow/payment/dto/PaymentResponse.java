package com.freelanceflow.payment.dto;

import com.freelanceflow.common.enums.PaymentStatus;
import com.freelanceflow.payment.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private Long invoiceId;
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant paidAt;
    private Instant createdAt;

    public PaymentResponse() {}

    public static PaymentResponse from(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.id = p.getId();
        if (p.getInvoice() != null) {
            r.invoiceId = p.getInvoice().getId();
        }
        r.razorpayPaymentId = p.getRazorpayPaymentId();
        r.razorpayOrderId = p.getRazorpayOrderId();
        r.amount = p.getAmount();
        r.status = p.getStatus();
        r.paidAt = p.getPaidAt();
        r.createdAt = p.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
}
