package com.rupp.payment.service;

import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.exception.DuplicatePaymentException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Business-level duplicate check (one active payment per Student_ID + Fee_Structure_ID); call before creating a payment
@Service
public class DuplicatePaymentGuard {

    private final PaymentRecords paymentRecords;
    private final PaymentAuditService auditService;

    public DuplicatePaymentGuard(PaymentRecords paymentRecords, PaymentAuditService auditService) {
        this.paymentRecords = paymentRecords;
        this.auditService = auditService;
    }

    // paymentId may be null for a not-yet-saved payment; an existing match on the same id is not a duplicate
    public void assertNoActivePayment(Long studentId, Long feeStructureId, Long paymentId) {
        Optional<Long> existing = paymentRecords.findActivePaymentId(studentId, feeStructureId);
        if (existing.isEmpty() || existing.get().equals(paymentId)) {
            return;
        }
        auditService.record(AuditAction.DUPLICATE_BLOCKED, paymentId,
                "studentId=%d feeStructureId=%d existingPaymentId=%d".formatted(studentId, feeStructureId, existing.get()));
        throw new DuplicatePaymentException(existing.get());
    }
}
