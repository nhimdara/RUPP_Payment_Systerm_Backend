package com.rupp.payment.repository;

import com.rupp.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    List<PaymentDetail> findByPaymentId(Long paymentId);
    Optional<PaymentDetail> findByTransactionRef(String transactionRef);
    boolean existsByTransactionRef(String transactionRef);
}
