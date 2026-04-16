package com.freelanceflow.invoice;

import com.freelanceflow.client.Client;
import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.user.User;
import com.freelanceflow.user.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public PdfGeneratorService(UserRepository userRepository, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        User user = userRepository.findById(invoice.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + invoice.getUserId()));
        Client client = clientRepository.findById(invoice.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + invoice.getClientId()));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Font
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Title
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Vendor & Client Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // From (Freelancer)
            String businessName = user.getBusinessName() != null && !user.getBusinessName().isBlank() ? user.getBusinessName() : user.getFullName();
            PdfPCell fromCell = new PdfPCell(new Phrase("From:\n" + businessName + "\n" + user.getEmail() + (user.getPhone() != null ? "\n" + user.getPhone() : ""), normalFont));
            fromCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(fromCell);

            // To (Client)
            PdfPCell toCell = new PdfPCell(new Phrase("To:\n" + client.getName() + (client.getCompany() != null ? "\n" + client.getCompany() : "") + "\n" + client.getEmail() + (client.getAddress() != null ? "\n" + client.getAddress() : ""), normalFont));
            toCell.setBorder(Rectangle.NO_BORDER);
            toCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(toCell);

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Invoice details
            Paragraph invoiceDetails = new Paragraph(
                    "Invoice #: " + String.format("INV-%06d", invoice.getInvoiceNumber()) + "\n" +
                    "Status: " + invoice.getStatus() + "\n" +
                    "Date: " + invoice.getCreatedAt().atZone(java.time.ZoneId.of("UTC")).format(DateTimeFormatter.ISO_LOCAL_DATE) + "\n" +
                    "Due Date: " + invoice.getDueDate(), normalFont);
            invoiceDetails.setAlignment(Element.ALIGN_RIGHT);
            document.add(invoiceDetails);
            document.add(Chunk.NEWLINE);

            // Line Items Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4f, 1f, 2f, 2f});
            itemsTable.setSpacingBefore(10f);

            // Table Header
            String[] headers = {"Description", "Qty", "Price", "Amount"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setPadding(5);
                itemsTable.addCell(cell);
            }

            // Table Rows
            for (InvoiceLineItem item : invoice.getLineItems()) {
                itemsTable.addCell(new Phrase(item.getDescription(), normalFont));
                itemsTable.addCell(new Phrase(item.getQuantity().toString(), normalFont));
                itemsTable.addCell(new Phrase("₹" + item.getUnitPrice().toString(), normalFont));
                itemsTable.addCell(new Phrase("₹" + item.getAmount().toString(), normalFont));
            }
            document.add(itemsTable);
            
            // Totals
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{7f, 3f});
            totalsTable.setSpacingBefore(10f);
            
            totalsTable.addCell(getCell("Subtotal:", Element.ALIGN_RIGHT, normalFont, Rectangle.NO_BORDER));
            totalsTable.addCell(getCell("₹" + invoice.getSubtotal().toString(), Element.ALIGN_RIGHT, normalFont, Rectangle.NO_BORDER));
            
            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalsTable.addCell(getCell("Tax (" + invoice.getTaxPercent() + "%):", Element.ALIGN_RIGHT, normalFont, Rectangle.NO_BORDER));
                totalsTable.addCell(getCell("₹" + invoice.getTaxAmount().toString(), Element.ALIGN_RIGHT, normalFont, Rectangle.NO_BORDER));
            }
            
            totalsTable.addCell(getCell("Total:", Element.ALIGN_RIGHT, headerFont, Rectangle.NO_BORDER));
            totalsTable.addCell(getCell("₹" + invoice.getTotal().toString(), Element.ALIGN_RIGHT, headerFont, Rectangle.NO_BORDER));
            
            document.add(totalsTable);
            document.add(Chunk.NEWLINE);

            if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
                document.add(new Paragraph("Notes:\n" + invoice.getNotes(), normalFont));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }
    }
    
    private PdfPCell getCell(String text, int alignment, Font font, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        cell.setPadding(5);
        return cell;
    }
}
