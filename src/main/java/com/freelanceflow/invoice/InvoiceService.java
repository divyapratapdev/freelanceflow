package com.freelanceflow.invoice;

import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.common.CacheConstants;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.dto.InvoiceLineItemRequest;
import com.freelanceflow.invoice.dto.InvoiceRequest;
import com.freelanceflow.invoice.dto.InvoiceResponse;
import com.freelanceflow.invoice.dto.InvoiceUpdateRequest;
import com.freelanceflow.project.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    /* 
     * [DESIGN NOTE] Architectural Choice:
     * We use a dedicated repository for persistence but decouple the long-running 
     * processes (Notification, PDF Generation) via Kafka to maintain a <200ms API response time.
     */
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          ProjectRepository projectRepository) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public InvoiceResponse create(Long userId, InvoiceRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            // client ownership check — catches cases where clientId belongs to a different user
            if (!clientRepository.existsByIdAndUserId(request.getClientId(), userId)) {
                throw new EntityNotFoundException("Client not found: " + request.getClientId());
            }
            if (request.getProjectId() != null) {
                if (projectRepository.findById(request.getProjectId())
                        .filter(p -> p.getUserId().equals(userId)).isEmpty()) {
                    throw new EntityNotFoundException("Project not found: " + request.getProjectId());
                }
            }

            // TODO: add GST type validation (IGST vs CGST+SGST) based on client state
            Invoice invoice = new Invoice();
            invoice.setUserId(userId);
            invoice.setClientId(request.getClientId());
            invoice.setProjectId(request.getProjectId());
            invoice.setDueDate(request.getDueDate());
            invoice.setNotes(request.getNotes());
            invoice.setTaxPercent(request.getTaxPercent() != null ? request.getTaxPercent() : BigDecimal.ZERO);
            
            invoice.setInvoiceNumber(invoiceRepository.nextInvoiceNumber());
            
            List<InvoiceLineItem> items = request.getLineItems().stream()
                    .map(this::mapLineItem)
                    .collect(Collectors.toList());
            invoice.setLineItems(items);
            
            calculateTotals(invoice);

            return InvoiceResponse.from(invoiceRepository.save(invoice));
        } finally {
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listAll(Long userId, InvoiceStatus status, Pageable pageable) {
        if (status != null) {
            return invoiceRepository.findByUserIdAndStatus(userId, status, pageable)
                    .map(InvoiceResponse::from);
        }
        return invoiceRepository.findByUserId(userId, pageable).map(InvoiceResponse::from);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long userId, Long invoiceId) {
        return InvoiceResponse.from(findAndVerifyOwnership(userId, invoiceId));
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public InvoiceResponse update(Long userId, Long invoiceId, InvoiceUpdateRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Invoice invoice = findAndVerifyOwnership(userId, invoiceId);
            
            if (invoice.getStatus() != InvoiceStatus.DRAFT) {
                throw new IllegalStateException("Only DRAFT invoices can be updated. Status is " + invoice.getStatus());
            }

            if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
            if (request.getNotes() != null) invoice.setNotes(request.getNotes());
            if (request.getTaxPercent() != null) invoice.setTaxPercent(request.getTaxPercent());

            if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
                List<InvoiceLineItem> items = request.getLineItems().stream()
                        .map(this::mapLineItem)
                        .collect(Collectors.toList());
                invoice.setLineItems(items);
            }
            
            calculateTotals(invoice);

            return InvoiceResponse.from(invoiceRepository.save(invoice));
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public InvoiceResponse markAsSent(Long userId, Long invoiceId, String razorpayLinkId, String razorpayLinkUrl) {
         MDC.put("userId", String.valueOf(userId));
         try {
             Invoice invoice = findAndVerifyOwnership(userId, invoiceId);
             
             if (invoice.getStatus() != InvoiceStatus.DRAFT) {
                 throw new IllegalStateException("Only DRAFT invoices can be marked as SENT. current status: " + invoice.getStatus());
             }
             
             invoice.setStatus(InvoiceStatus.SENT);
             invoice.setRazorpayPaymentLinkId(razorpayLinkId);
             invoice.setRazorpayPaymentLinkUrl(razorpayLinkUrl);
             
             return InvoiceResponse.from(invoiceRepository.save(invoice));
         } finally {
             MDC.clear();
         }
    }
    
    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public void markAsCancelled(Long userId, Long invoiceId) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Invoice invoice = findAndVerifyOwnership(userId, invoiceId);
            if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
                 throw new IllegalStateException("Cannot cancel a PAID or already CANCELLED invoice.");
            }
            // Deactivation of Razorpay link should ideally happen here or in a calling orchestration service.
            invoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(invoice);
        } finally {
            MDC.clear();
        }
    }

    private InvoiceLineItem mapLineItem(InvoiceLineItemRequest req) {
        return new InvoiceLineItem(req.getDescription(), req.getQuantity(), req.getUnitPrice());
    }

    private void calculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getLineItems().stream()
                .map(InvoiceLineItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setSubtotal(subtotal);

        /* 
         * [DESIGN NOTE] Floating-point precision:
         * We use BigDecimal throughout the engine to avoid IEEE 754 rounding errors 
         * which are critical in financial SaaS applications.
         */
        BigDecimal taxAmount = subtotal.multiply(invoice.getTaxPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);

        invoice.setTotal(subtotal.add(taxAmount));
    }

    public Invoice findAndVerifyOwnership(Long userId, Long invoiceId) {
        return invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found or access denied: " + invoiceId));
    }
}
