package com.rupp.payment.service;

import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.exception.PaymentNotFoundException;
import com.rupp.payment.exception.PaymentStatusConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

// Finance Staff side of "Verify / Confirm Payment": only a payment the student already verified by OTP can be confirmed
@Service
public class PaymentConfirmationService {

    private static final Set<String> CONFIRMABLE = Set.of(PaymentStatusName.VERIFIED);
    private static final Set<String> REJECTABLE = Set.of(PaymentStatusName.PENDING, PaymentStatusName.VERIFIED);

    private final PaymentRecords paymentRecords;
    private final PaymentAuditService auditService;

    public PaymentConfirmationService(PaymentRecords paymentRecords, PaymentAuditService auditService) {
        this.paymentRecords = paymentRecords;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentStatusResponse confirm(Long paymentId, String note) {
        String details = note == null || note.isBlank() ? "Confirmed by finance staff" : note;
        return transition(paymentId, CONFIRMABLE, PaymentStatusName.CONFIRMED, details);
    }

    @Transactional
    public PaymentStatusResponse reject(Long paymentId, String reason) {
        return transition(paymentId, REJECTABLE, PaymentStatusName.REJECTED, reason);
    }

    private PaymentStatusResponse transition(Long paymentId, Set<String> allowedFrom, String to, String details) {
        PaymentSnapshot payment = paymentRecords.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
        String from = payment.status();
        if (!allowedFrom.contains(from)) {
            throw new PaymentStatusConflictException(from);
        }
        // Two staff acting on the same payment at once: only one compare-and-set wins
        if (!paymentRecords.updateStatus(paymentId, from, to)) {
            throw new PaymentStatusConflictException(
                    paymentRecords.findById(paymentId).map(PaymentSnapshot::status).orElse(from));
        }
        auditService.recordStatusChange(paymentId, from, to, details);
        return new PaymentStatusResponse(paymentId, to);
    }
}
