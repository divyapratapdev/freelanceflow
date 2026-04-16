package com.freelanceflow.notification;

import com.freelanceflow.common.KafkaTopics;
import com.freelanceflow.notification.dto.InvoiceSentEvent;
import com.freelanceflow.notification.dto.PaymentReceivedEvent;
import com.freelanceflow.notification.dto.ReminderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final NotificationService notificationService;

    public KafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.INVOICE_SENT)
    public void listenInvoiceSent(InvoiceSentEvent event) {
        log.info("Received InvoiceSentEvent for invoiceId={}", event.getInvoiceId());
        try {
            notificationService.processInvoiceSent(event);
        } catch (Exception e) {
            log.error("Error processing InvoiceSentEvent (caught to prevent infinite loop)", e);
            // KafkaConsumer NEVER rethrows — catch, log, done
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_RECEIVED)
    public void listenPaymentReceived(PaymentReceivedEvent event) {
        log.info("Received PaymentReceivedEvent for invoiceId={}", event.getInvoiceId());
        try {
            notificationService.processPaymentReceived(event);
        } catch (Exception e) {
            log.error("Error processing PaymentReceivedEvent", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.REMINDER)
    public void listenReminder(ReminderEvent event) {
        log.info("Received ReminderEvent of type {} for invoiceId={}", event.getReminderType(), event.getInvoiceId());
        try {
            notificationService.processReminder(event);
        } catch (Exception e) {
            log.error("Error processing ReminderEvent", e);
        }
    }
}
