package com.freelanceflow.notification;

import com.freelanceflow.client.Client;
import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.common.enums.ReminderType;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import com.freelanceflow.invoice.PdfGeneratorService;
import com.freelanceflow.notification.dto.InvoiceSentEvent;
import com.freelanceflow.notification.dto.PaymentReceivedEvent;
import com.freelanceflow.notification.dto.ReminderEvent;
import com.freelanceflow.user.User;
import com.freelanceflow.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public NotificationService(EmailService emailService,
                               PdfGeneratorService pdfGeneratorService,
                               InvoiceRepository invoiceRepository,
                               UserRepository userRepository,
                               ClientRepository clientRepository) {
        this.emailService = emailService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public void processInvoiceSent(InvoiceSentEvent event) {
        Invoice invoice = getInvoice(event.getInvoiceId());
        User user = getUser(event.getUserId());
        Client client = getClient(event.getClientId());

        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);
        
        String invNumber = String.format("INV-%06d", invoice.getInvoiceNumber());
        String freelancerName = user.getBusinessName() != null && !user.getBusinessName().isBlank() ? user.getBusinessName() : user.getFullName();
        String paymentLink = invoice.getRazorpayPaymentLinkUrl() != null ? invoice.getRazorpayPaymentLinkUrl() : "#";
        
        String subject = "Invoice " + invNumber + " from " + freelancerName + " — Rs." + invoice.getTotal();
        String body = buildHtmlTemplate(
            "New Invoice Received",
            "Dear " + client.getName() + ", you have received a new invoice.",
            "Amount Due: <strong>Rs." + invoice.getTotal() + "</strong><br/>Due Date: " + invoice.getDueDate(),
            paymentLink,
            "Pay Invoice",
            freelancerName
        );

        emailService.sendEmailWithAttachment(client.getEmail(), subject, body, true, pdfBytes, "invoice-" + invNumber + ".pdf");
        log.info("Processed InvoiceSentEvent for Invoice ID {}", event.getInvoiceId());
    }

    public void processPaymentReceived(PaymentReceivedEvent event) {
        Invoice invoice = getInvoice(event.getInvoiceId());
        User user = getUser(event.getUserId());
        Client client = getClient(event.getClientId());
        
        String invNumber = String.format("INV-%06d", invoice.getInvoiceNumber());
        BigDecimal amount = event.getAmount();
        String freelancerName = user.getBusinessName() != null && !user.getBusinessName().isBlank() ? user.getBusinessName() : user.getFullName();

        // 1. Email to Freelancer
        String freelancerSubject = "Payment received — Invoice " + invNumber + " — Rs." + amount;
        String freelancerBody = buildHtmlTemplate(
            "Payment Received! 🎉",
            client.getName() + " just paid Rs." + amount + " for invoice " + invNumber + ".",
            "Your Razorpay account has been credited.",
            "#",
            "View Dashboard",
            "FreelanceFlow System"
        );
        emailService.sendEmail(user.getEmail(), freelancerSubject, freelancerBody, true);

        // 2. Email to Client
        String clientSubject = "Payment confirmed — Invoice " + invNumber;
        String clientBody = buildHtmlTemplate(
            "Payment Confirmed",
            "Dear " + client.getName() + ", thank you for your payment.",
            "We have safely received Rs." + amount + ". Invoice " + invNumber + " is now closed.",
            null, null, freelancerName
        );
        emailService.sendEmail(client.getEmail(), clientSubject, clientBody, true);
        
        log.info("Processed PaymentReceivedEvent for Invoice ID {}", event.getInvoiceId());
    }

    public void processReminder(ReminderEvent event) {
        Invoice invoice = getInvoice(event.getInvoiceId());
        Client client = getClient(event.getClientId());
        User user = getUser(invoice.getUserId());
        
        String invNumber = String.format("INV-%06d", invoice.getInvoiceNumber());
        String link = invoice.getRazorpayPaymentLinkUrl() != null ? invoice.getRazorpayPaymentLinkUrl() : "#";
        String freelancerName = user.getBusinessName() != null && !user.getBusinessName().isBlank() ? user.getBusinessName() : user.getFullName();
        
        String subject;
        String body;

        if (event.getReminderType() == ReminderType.THREE_DAY_REMINDER) {
            subject = "Reminder: Invoice " + invNumber + " due in 3 days — Rs." + invoice.getTotal();
            body = buildHtmlTemplate("Upcoming Due Date", "Dear " + client.getName() + ",",
                   "Invoice " + invNumber + " for Rs." + invoice.getTotal() + " is due on " + invoice.getDueDate() + ".",
                   link, "Pay Now", freelancerName);
        } else { // OVERDUE_ALERT
            subject = "OVERDUE: Invoice " + invNumber + " — Rs." + invoice.getTotal() + " past due";
            body = buildHtmlTemplate("Overdue Alert", "Dear " + client.getName() + ",",
                   "Invoice " + invNumber + " for Rs." + invoice.getTotal() + " was due on " + invoice.getDueDate() + ". Please settle this immediately.",
                   link, "Pay Immediately", freelancerName);
        }

        emailService.sendEmail(client.getEmail(), subject, body, true);
        log.info("Processed ReminderEvent ({}) for Invoice ID {}", event.getReminderType(), event.getInvoiceId());
    }

    private String buildHtmlTemplate(String header, String p1, String p2, String btnLink, String btnText, String footerStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f9fafb; padding: 20px; border-radius: 8px;'>");
        sb.append("<div style='background-color: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>");
        sb.append("<h2 style='margin: 0;'>").append(header).append("</h2></div>");
        sb.append("<div style='background-color: white; padding: 30px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;'>");
        sb.append("<p style='font-size: 16px; color: #374151;'>").append(p1).append("</p>");
        sb.append("<p style='font-size: 16px; color: #374151; padding: 15px; background: #f3f4f6; border-radius: 6px;'>").append(p2).append("</p>");
        if (btnLink != null && btnText != null) {
            sb.append("<div style='text-align: center; margin-top: 30px; margin-bottom: 30px;'>");
            sb.append("<a href='").append(btnLink).append("' style='background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>").append(btnText).append("</a>");
            sb.append("</div>");
        }
        sb.append("<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'/>");
        sb.append("<p style='font-size: 14px; color: #6b7280; margin: 0;'>Thank you,</p>");
        sb.append("<p style='font-size: 16px; font-weight: bold; color: #111827; margin: 5px 0 0 0;'>").append(footerStr).append("</p>");
        sb.append("</div></div>");
        return sb.toString();
    }

    private Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
    }
    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    private Client getClient(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client not found"));
    }
}
