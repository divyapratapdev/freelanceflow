package com.freelanceflow.invoice;

import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.KafkaTopics;
import com.freelanceflow.common.PageResponse;
import com.freelanceflow.common.SecurityUtils;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.dto.InvoiceRequest;
import com.freelanceflow.invoice.dto.InvoiceResponse;
import com.freelanceflow.invoice.dto.InvoiceUpdateRequest;
import com.freelanceflow.notification.KafkaProducer;
import com.freelanceflow.notification.dto.InvoiceSentEvent;
import com.freelanceflow.payment.PaymentService;
import com.freelanceflow.payment.dto.PaymentLinkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice and PDF management")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGeneratorService pdfGeneratorService;
    private final PaymentService paymentService;
    private final KafkaProducer kafkaProducer;

    public InvoiceController(InvoiceService invoiceService, 
                             PdfGeneratorService pdfGeneratorService,
                             PaymentService paymentService,
                             KafkaProducer kafkaProducer) {
        this.invoiceService = invoiceService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.paymentService = paymentService;
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping
    @Operation(summary = "Create a new draft invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@RequestBody @Valid InvoiceRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(invoiceService.create(userId, request)));
    }

    @GetMapping
    @Operation(summary = "List all invoices (paginated), optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> listAll(
            @RequestParam(required = false) InvoiceStatus status,
            Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.of(invoiceService.listAll(userId, status, pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getById(userId, id)));
    }
    
    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download invoice PDF")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Invoice invoice = invoiceService.findAndVerifyOwnership(userId, id);
        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + invoice.getInvoiceNumber() + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update draft invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid InvoiceUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.update(userId, id, request)));
    }
    
    @PostMapping("/{id}/send")
    @Operation(summary = "Send invoice to client via email and create Razorpay payment link")
    public ResponseEntity<ApiResponse<InvoiceResponse>> sendInvoice(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 1. Generate Razorpay Link
        PaymentLinkResponse linkResponse = paymentService.createPaymentLink(userId, id);
        
        // 2. Update Invoice Status to SENT and save link details
        InvoiceResponse response = invoiceService.markAsSent(userId, id, linkResponse.getLinkId(), linkResponse.getLinkUrl());
        
        // 3. Publish to Kafka (Email sending is async)
        InvoiceSentEvent event = new InvoiceSentEvent(id, userId, response.getClientId());
        kafkaProducer.sendMessage(KafkaTopics.INVOICE_SENT, String.valueOf(id), event);
        
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel invoice")
    public ResponseEntity<ApiResponse<Void>> cancelInvoice(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Invoice invoice = invoiceService.findAndVerifyOwnership(userId, id);
        
        // Deactivate razorpay link if it was sent
        if (invoice.getStatus() == InvoiceStatus.SENT && invoice.getRazorpayPaymentLinkId() != null) {
            paymentService.cancelPaymentLink(invoice.getRazorpayPaymentLinkId());
        }
        
        invoiceService.markAsCancelled(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
