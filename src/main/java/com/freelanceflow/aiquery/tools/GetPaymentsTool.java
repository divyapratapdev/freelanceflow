package com.freelanceflow.aiquery.tools;

import com.freelanceflow.payment.PaymentRepository;
import com.freelanceflow.payment.dto.PaymentResponse;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service("getPaymentsTool")
@Description("Fetch all payments received by the current user.")
public class GetPaymentsTool implements Function<Long, List<PaymentResponse>> {

    private final PaymentRepository paymentRepository;

    public GetPaymentsTool(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<PaymentResponse> apply(Long userId) {
        return paymentRepository.findByInvoice_UserId(userId, Pageable.unpaged())
                .stream().map(PaymentResponse::from).toList();
    }
}
