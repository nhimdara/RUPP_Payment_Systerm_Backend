package com.rupp.payment.repository;

import com.rupp.payment.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long>, JpaSpecificationExecutor<Receipt> {
    Optional<Receipt> findByPaymentId(Long paymentId);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    boolean existsByReceiptNumber(String receiptNumber);
}
