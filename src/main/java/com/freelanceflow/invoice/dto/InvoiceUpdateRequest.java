package com.freelanceflow.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceUpdateRequest {

    private List<InvoiceLineItemRequest> lineItems;
    private BigDecimal taxPercent;
    private LocalDate dueDate;
    private String notes;

    public InvoiceUpdateRequest() {}

    public List<InvoiceLineItemRequest> getLineItems() { return lineItems; }
    public void setLineItems(List<InvoiceLineItemRequest> lineItems) { this.lineItems = lineItems; }

    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
