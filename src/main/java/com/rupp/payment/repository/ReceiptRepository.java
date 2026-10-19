package com.rupp.payment.repository;

import com.rupp.payment.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long>, JpaSpecificationExecutor<Receipt> {

    Optional<Receipt> findByPaymentId(Long paymentId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    boolean existsByPaymentId(Long paymentId);

=======
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByPaymentId(Long paymentId);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
>>>>>>> origin/feature/search
    boolean existsByReceiptNumber(String receiptNumber);
}
