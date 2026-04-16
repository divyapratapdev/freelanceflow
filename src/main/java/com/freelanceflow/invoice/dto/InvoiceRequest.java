package com.freelanceflow.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import com.freelanceflow.invoice.dto.InvoiceLineItemRequest;

@Schema(description = "Payload for generating a new Invoice")
public class InvoiceRequest {

    @NotNull
    @Schema(description = "Primary ID linking to the client being billed", example = "42")
    private Long clientId;

    @Schema(description = "Optional ID linking to a specific project", example = "105")
    private Long projectId;

    @FutureOrPresent
    @NotNull
    @Schema(description = "Due date of the invoice payment", example = "2026-12-31")
    private LocalDate dueDate;

    @Schema(description = "Tax percentage applied to subtotal", example = "18.00")
    private BigDecimal taxPercent;

    @Schema(description = "Additional notes", example = "Thank you for your business")
    private String notes;

    @NotEmpty
    @Schema(description = "One or more line items detailing the billable work")
    private List<InvoiceLineItemRequest> lineItems;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<InvoiceLineItemRequest> getLineItems() { return lineItems; }
    public void setLineItems(List<InvoiceLineItemRequest> lineItems) { this.lineItems = lineItems; }
}
