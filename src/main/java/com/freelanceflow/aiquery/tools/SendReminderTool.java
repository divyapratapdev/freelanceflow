package com.freelanceflow.aiquery.tools;

import com.freelanceflow.common.KafkaTopics;
import com.freelanceflow.common.enums.ReminderType;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceService;
import com.freelanceflow.notification.KafkaProducer;
import com.freelanceflow.notification.NotificationService;
import com.freelanceflow.notification.dto.ReminderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service("sendReminderTool")
@Description("Send a reminder for a specific invoice ID. specify invoiceId as argument.")
public class SendReminderTool implements Function<SendReminderTool.Request, String> {

    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final KafkaProducer kafkaProducer;
    private final boolean kafkaEnabled;

    public SendReminderTool(InvoiceService invoiceService, 
                            NotificationService notificationService,
                            KafkaProducer kafkaProducer,
                            @Value("${app.kafka.enabled:true}") boolean kafkaEnabled) {
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
        this.kafkaProducer = kafkaProducer;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Override
    public String apply(Request request) {
        try {
            Invoice invoice = invoiceService.findAndVerifyOwnership(request.userId(), request.invoiceId());
            ReminderEvent event = new ReminderEvent(invoice.getId(), invoice.getUserId(), invoice.getClientId(), ReminderType.OVERDUE_ALERT);
            
            if (kafkaEnabled) {
                kafkaProducer.sendMessage(KafkaTopics.REMINDER, String.valueOf(invoice.getId()), event);
            } else {
                notificationService.processReminder(event);
            }
            return "Reminder sent successfully for invoice " + request.invoiceId();
        } catch (Exception e) {
            return "Failed to send reminder for invoice " + request.invoiceId() + ": " + e.getMessage();
        }
    }

    public record Request(Long userId, Long invoiceId) {}
}
