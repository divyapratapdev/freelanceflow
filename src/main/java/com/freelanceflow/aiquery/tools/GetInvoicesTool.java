package com.freelanceflow.aiquery.tools;

import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.InvoiceService;
import com.freelanceflow.invoice.dto.InvoiceResponse;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service("getInvoicesTool")
@Description("Fetch invoices for the current user, optionally filtered by status if the status is provided.")
public class GetInvoicesTool implements Function<GetInvoicesTool.Request, List<InvoiceResponse>> {

    private final InvoiceService invoiceService;

    public GetInvoicesTool(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public List<InvoiceResponse> apply(Request request) {
        InvoiceStatus statusObj = null;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                statusObj = InvoiceStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status for tool
            }
        }
        return invoiceService.listAll(request.userId(), statusObj, Pageable.unpaged()).getContent();
    }

    public record Request(Long userId, String status) {}
}
