package com.freelanceflow.payment;

import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.PageResponse;
import com.freelanceflow.common.SecurityUtils;
import com.freelanceflow.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "View payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    @Operation(summary = "List all payments for current user")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> listAll(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Uses the findByInvoice_UserId method
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.of(paymentRepository.findByInvoice_UserId(userId, pageable).map(PaymentResponse::from))));
    }
}
