package com.freelanceflow.invoice;

import java.math.BigDecimal;

/**
 * Plain POJO — NOT an @Entity.
 * Stored as JSONB in the invoices.line_items column via InvoiceLineItemConverter.
 */
public class InvoiceLineItem {

    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount; // quantity * unitPrice

    public InvoiceLineItem() {}

    public InvoiceLineItem(String description, BigDecimal quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity.multiply(unitPrice);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
