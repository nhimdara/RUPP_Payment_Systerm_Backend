package com.rupp.payment.repository;

import com.rupp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByStudentId(Long studentId);

    List<Payment> findByStudentUserIdOrderByPaymentDateDesc(Long userId);

    List<Payment> findByPaymentStatusId(Long statusId);

    List<Payment> findByPaymentStatusStatusName(String statusName);

    boolean existsByFeeStructureId(Long feeStructureId);

    @Query("SELECT COALESCE(SUM(p.totalPaid), 0) FROM Payment p WHERE p.paymentStatus.statusName = 'CONFIRMED'")
    BigDecimal calculateTotalConfirmedRevenue();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus.statusName = 'PENDING'")
    long countPendingPayments();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus.statusName = 'CONFIRMED'")
    long countConfirmedPayments();
}
