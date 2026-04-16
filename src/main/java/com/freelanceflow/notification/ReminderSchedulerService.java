package com.freelanceflow.notification;

import com.freelanceflow.common.KafkaTopics;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.common.enums.ReminderType;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import com.freelanceflow.notification.dto.ReminderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final InvoiceRepository invoiceRepository;
    private final KafkaProducer kafkaProducer;

    public ReminderSchedulerService(InvoiceRepository invoiceRepository, KafkaProducer kafkaProducer) {
        this.invoiceRepository = invoiceRepository;
        this.kafkaProducer = kafkaProducer;
    }

    // Run every day at 9:00 AM IST
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void sendThreeDayReminders() {
        log.info("Starting 3-day reminder scheduler...");
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        // Find invoices exactly 3 days from due
        List<Invoice> invoicesToRemind = invoiceRepository.findByStatusAndDueDateBetween(
                InvoiceStatus.SENT, threeDaysFromNow, threeDaysFromNow);

        int count = 0;
        for (Invoice invoice : invoicesToRemind) {
            ReminderEvent event = new ReminderEvent(
                    invoice.getId(), invoice.getUserId(), invoice.getClientId(), ReminderType.THREE_DAY_REMINDER);
            kafkaProducer.sendMessage(KafkaTopics.REMINDER, String.valueOf(invoice.getId()), event);
            count++;
        }
        log.info("Sent 3-day reminders for {} invoices.", count);
    }

    // Run every day at 10:00 AM IST
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processOverdueInvoices() {
        log.info("Starting overdue invoice scheduler...");
        LocalDate today = LocalDate.now();
        List<Invoice> overdueCandidates = invoiceRepository.findByStatusAndDueDateBefore(
                InvoiceStatus.SENT, today);

        int count = 0;
        for (Invoice invoice : overdueCandidates) {
            // State transition
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);

            ReminderEvent event = new ReminderEvent(
                    invoice.getId(), invoice.getUserId(), invoice.getClientId(), ReminderType.OVERDUE_ALERT);
            kafkaProducer.sendMessage(KafkaTopics.REMINDER, String.valueOf(invoice.getId()), event);
            count++;
        }
        log.info("Marked {} invoices as overdue and sent alerts.", count);
    }
}
