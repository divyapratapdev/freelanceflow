package com.freelanceflow.payment;

import com.freelanceflow.common.CacheConstants;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import com.freelanceflow.payment.dto.PaymentLinkResponse;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final RazorpayClient razorpayClient;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(RazorpayClient razorpayClient, InvoiceRepository invoiceRepository) {
        this.razorpayClient = razorpayClient;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public PaymentLinkResponse createPaymentLink(Long userId, Long invoiceId) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Invoice invoice = findAndVerifyOwnership(userId, invoiceId);

            JSONObject paymentLinkRequest = new JSONObject();
            // Amount in paise: Rs.1000 = 100000
            int amountInPaise = invoice.getTotal().multiply(new BigDecimal("100")).intValueExact();
            paymentLinkRequest.put("amount", amountInPaise);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", false);
            paymentLinkRequest.put("description", "Payment for Invoice INV-" + String.format("%06d", invoice.getInvoiceNumber()));
            
            // Set expiry correctly (e.g. 15 mins after create at least)
            // Razorpay expect timestamp. Given the rules, we just set a reasonable expiry 
            long expireBy = Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond();
            paymentLinkRequest.put("expire_by", expireBy);
            
            JSONObject customer = new JSONObject();
            customer.put("name", invoice.getUserId().toString()); // Mocking some name
            customer.put("email", "client@example.com"); // We will use generic for now or fetch client email
            // Alternatively, can skip customer details if not mandatory, but usually it's good to have.
            
            JSONObject notes = new JSONObject();
            notes.put("invoiceId", String.valueOf(invoice.getId()));
            paymentLinkRequest.put("notes", notes);

            PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);

            return new PaymentLinkResponse(paymentLink.get("id"), paymentLink.get("short_url"));
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay payment link", e);
            throw new RuntimeException("Payment provider error: " + e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    public void cancelPaymentLink(String paymentLinkId) {
        try {
            razorpayClient.paymentLink.cancel(paymentLinkId);
            log.info("Cancelled Razorpay payment link {}", paymentLinkId);
        } catch (RazorpayException e) {
            log.error("Failed to cancel Razorpay payment link {}", paymentLinkId, e);
            // We log but don't strictly throw to avoid failing the whole cancellation if Razorpay already cancelled 
        }
    }

    private Invoice findAndVerifyOwnership(Long userId, Long invoiceId) {
        return invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found or access denied: " + invoiceId));
    }
}
