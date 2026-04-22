package com.freelanceflow.payment;

import com.freelanceflow.common.CacheConstants;
import com.freelanceflow.common.KafkaTopics;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.common.enums.PaymentStatus;
import com.freelanceflow.config.RazorpayConfig;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import com.freelanceflow.notification.KafkaProducer;
import com.freelanceflow.notification.dto.PaymentReceivedEvent;
import com.razorpay.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
public class RazorpayWebhookHandler {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookHandler.class);

    private final RazorpayConfig razorpayConfig;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final KafkaProducer kafkaProducer;
    private final com.freelanceflow.common.IdempotencyService idempotencyService;
    private final org.springframework.context.ApplicationContext applicationContext;

    public RazorpayWebhookHandler(RazorpayConfig razorpayConfig,
                                  PaymentRepository paymentRepository,
                                  InvoiceRepository invoiceRepository,
                                  KafkaProducer kafkaProducer,
                                  com.freelanceflow.common.IdempotencyService idempotencyService,
                                  org.springframework.context.ApplicationContext applicationContext) {
        this.razorpayConfig = razorpayConfig;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.kafkaProducer = kafkaProducer;
        this.idempotencyService = idempotencyService;
        this.applicationContext = applicationContext;
    }

    @PostMapping("/api/payments/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] payloadBytes,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        String payload = new String(payloadBytes);
        try {
            boolean isSignatureValid = Utils.verifyWebhookSignature(payload, signature, razorpayConfig.getWebhookSecret());
            if (!isSignatureValid) {
                log.warn("Invalid Razorpay webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            if ("payment.captured".equals(event) || "payment_link.paid".equals(event)) {
                JSONObject paymentEntity;
                if ("payment.captured".equals(event)) {
                    paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                } else {
                    if (json.getJSONObject("payload").has("payment")) {
                        paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                    } else {
                        return ResponseEntity.ok("OK");
                    }
                }

                String razorpayPaymentId = paymentEntity.getString("id");
                
                // --- EXPERT RESILIENCE: Redis Idempotency Lock ---
                if (!idempotencyService.acquire(razorpayPaymentId, java.time.Duration.ofHours(24))) {
                    log.info("Duplicate webhook detected via Redis for paymentId: {}, returning OK early", razorpayPaymentId);
                    return ResponseEntity.ok("OK");
                }
                // ------------------------------------------------

                String orderId = paymentEntity.optString("order_id", null);
                
                JSONObject notes = paymentEntity.optJSONObject("notes");
                if (notes == null || !notes.has("invoiceId")) {
                    log.error("Missing invoiceId in Razorpay payment notes. PaymentId: {}", razorpayPaymentId);
                    return ResponseEntity.ok("OK");
                }

                Long invoiceId = Long.parseLong(notes.getString("invoiceId"));

                Invoice invoice = invoiceRepository.findById(invoiceId)
                        .orElseThrow(() -> new EntityNotFoundException("Invoice not found for webhook: " + invoiceId));
                
                // Evict Dashboard cache for this user by invoking through the proxy
                applicationContext.getBean(RazorpayWebhookHandler.class).evictDashboardCache(invoice.getUserId());

                Payment payment = new Payment();
                payment.setInvoice(invoice);
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpayOrderId(orderId);
                // Convert paise to Rs
                payment.setAmount(new BigDecimal(paymentEntity.getInt("amount")).divide(new BigDecimal("100")));
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(Instant.now());

                try {
                    paymentRepository.save(payment);
                } catch (DataIntegrityViolationException ex) {
                    log.info("Duplicate webhook event mapped to duplicate payment save, ignoring smoothly: {}", razorpayPaymentId);
                    return ResponseEntity.ok("OK");
                }

                if (invoice.getStatus() != InvoiceStatus.PAID) {
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoiceRepository.save(invoice);
                }

                // Send Kafka notification
                PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                        invoice.getId(), invoice.getUserId(), invoice.getClientId(), payment.getAmount());
                kafkaProducer.sendMessage(KafkaTopics.PAYMENT_RECEIVED, String.valueOf(invoice.getId()), paymentEvent);
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            // Always return 200 for Razorpay to prevent infinite retries unless it's genuinely transient
            return ResponseEntity.ok("OK");
        }
    }
    
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public void evictDashboardCache(Long userId) {
        // Simple method to force cache eviction
    }
}
