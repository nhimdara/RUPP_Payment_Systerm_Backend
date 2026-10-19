package com.rupp.payment.repository;

import com.rupp.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    Optional<PaymentStatus> findByStatusName(String statusName);
    boolean existsByStatusName(String statusName);
}
