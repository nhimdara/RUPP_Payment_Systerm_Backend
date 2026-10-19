package com.rupp.payment.service;

import com.rupp.payment.dto.request.DevPaymentCreateRequest;
import com.rupp.payment.dto.response.DevPaymentResponse;
import com.rupp.payment.entity.AuditAction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

// Dev-only "Make Payment" stand-in so the OTP and confirmation flow can be exercised before the Payment API exists
@Service
@ConditionalOnProperty(name = "payment.security.dev-endpoints.enabled", havingValue = "true")
public class DevPaymentService {

    private final PaymentOwnership ownership;
    private final DuplicatePaymentGuard duplicateGuard;
    private final InMemoryPaymentRecords payments;
    private final PaymentAuditService auditService;
    private final Clock clock;

    public DevPaymentService(PaymentOwnership ownership, DuplicatePaymentGuard duplicateGuard,
                             InMemoryPaymentRecords payments, PaymentAuditService auditService, Clock clock) {
        this.ownership = ownership;
        this.duplicateGuard = duplicateGuard;
        this.payments = payments;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public DevPaymentResponse create(DevPaymentCreateRequest request) {
        Long studentId = ownership.currentStudentId();
        PaymentSnapshot payment;
        // Check-then-create must be atomic; the real Payment table should back this with a partial unique index
        synchronized (payments) {
            duplicateGuard.assertNoActivePayment(studentId, request.feeStructureId(), null);
            payment = payments.createPending(studentId, request.feeStructureId());
        }
        auditService.record(AuditAction.PAYMENT_CREATED, payment.paymentId(),
                "feeStructureId=%d method=%s transactionRef=%s amount=%s".formatted(
                        request.feeStructureId(), request.method(), request.transactionRef(), request.amount()));
        return new DevPaymentResponse(payment.paymentId(), studentId, payment.feeStructureId(), payment.status(),
                request.method(), request.transactionRef(), request.amount(), clock.instant());
    }
}
