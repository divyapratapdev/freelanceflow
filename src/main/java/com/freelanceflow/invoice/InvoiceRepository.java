package com.freelanceflow.invoice;

import com.freelanceflow.common.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    Page<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status, Pageable pageable);

    Optional<Invoice> findByIdAndUserId(Long id, Long userId);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatusAndDueDateBetween(InvoiceStatus status, LocalDate from, LocalDate to);

    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    Long nextInvoiceNumber();

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.userId = :userId AND i.status = 'PAID'")
    java.math.BigDecimal sumPaidByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.userId = :userId " +
           "AND i.status IN ('SENT', 'OVERDUE')")
    java.math.BigDecimal sumOutstandingByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.userId = :userId AND i.status = 'OVERDUE'")
    java.math.BigDecimal sumOverdueByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.userId = :userId AND i.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);
}
