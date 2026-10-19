package com.rupp.payment.repository;

import com.rupp.payment.entity.PaymentAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;

// Deliberately exposes no update/delete methods
public interface PaymentAuditLogRepository extends Repository<PaymentAuditLog, Long> {

    PaymentAuditLog save(PaymentAuditLog log);

    Page<PaymentAuditLog> findAll(Specification<PaymentAuditLog> spec, Pageable pageable);
}
