package com.freelanceflow.invoice;

import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.invoice.dto.InvoiceLineItemRequest;
import com.freelanceflow.invoice.dto.InvoiceRequest;
import com.freelanceflow.invoice.dto.InvoiceResponse;
import com.freelanceflow.project.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void create_ShouldCalculateTotalsCorrectly() {
        // Given
        Long userId = 1L;
        InvoiceRequest request = new InvoiceRequest();
        request.setClientId(2L);
        request.setTaxPercent(new BigDecimal("18"));
        request.setDueDate(LocalDate.now().plusDays(7));
        
        InvoiceLineItemRequest item = new InvoiceLineItemRequest();
        item.setDescription("Logo Design");
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("1000"));
        request.setLineItems(List.of(item));

        when(clientRepository.existsByIdAndUserId(request.getClientId(), userId)).thenReturn(true);
        when(invoiceRepository.nextInvoiceNumber()).thenReturn(101L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        // When
        InvoiceResponse response = invoiceService.create(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSubtotal()).isEqualByComparingTo("1000");
        assertThat(response.getTaxAmount()).isEqualByComparingTo("180");
        assertThat(response.getTotal()).isEqualByComparingTo("1180");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void create_ShouldThrowException_WhenClientNotFound() {
        // Given
        Long userId = 1L;
        InvoiceRequest request = new InvoiceRequest();
        request.setClientId(999L);

        when(clientRepository.existsByIdAndUserId(999L, userId)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> invoiceService.create(userId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    void markAsSent_ShouldChangeStatus_WhenInvoiceIsDraft() {
        // Given
        Long userId = 1L;
        Long invoiceId = 100L;
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setUserId(userId);
        invoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.findByIdAndUserId(invoiceId, userId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        // When
        InvoiceResponse response = invoiceService.markAsSent(userId, invoiceId, "pay_123", "http://rpy.link/123");

        // Then
        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(response.getRazorpayPaymentLinkId()).isEqualTo("pay_123");
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void markAsSent_ShouldThrowException_WhenInvoiceIsAlreadySent() {
        // Given
        Long userId = 1L;
        Long invoiceId = 100L;
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setUserId(userId);
        invoice.setStatus(InvoiceStatus.SENT);

        when(invoiceRepository.findByIdAndUserId(invoiceId, userId)).thenReturn(Optional.of(invoice));

        // When / Then
        assertThatThrownBy(() -> invoiceService.markAsSent(userId, invoiceId, "pk_123", "url"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT invoices can be marked as SENT");
    }

    @Test
    void findAndVerifyOwnership_ShouldThrowException_WhenAccessDenied() {
        // Given
        Long userId = 1L;
        Long invoiceId = 100L;
        when(invoiceRepository.findByIdAndUserId(invoiceId, userId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> invoiceService.findAndVerifyOwnership(userId, invoiceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("access denied");
    }
}
