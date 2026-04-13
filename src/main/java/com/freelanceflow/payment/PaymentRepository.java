package com.freelanceflow.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByInvoice_UserId(Long userId, Pageable pageable);
    
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
}
