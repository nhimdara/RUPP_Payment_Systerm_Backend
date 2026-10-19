package com.rupp.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Dev stand-in until the Payment entity is on main; payments live only in memory and vanish on restart
@Component
public class InMemoryPaymentRecords implements PaymentRecords {

    private static final Logger log = LoggerFactory.getLogger(InMemoryPaymentRecords.class);

    private final Map<Long, PaymentSnapshot> payments = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public InMemoryPaymentRecords() {
        log.warn("InMemoryPaymentRecords is active: payments are kept in memory. Replace with a Payment-repository-backed PaymentRecords.");
    }

    @Override
    public Optional<PaymentSnapshot> findById(Long paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public Optional<Long> findActivePaymentId(Long studentId, Long feeStructureId) {
        return payments.values().stream()
                .filter(payment -> payment.studentId().equals(studentId)
                        && payment.feeStructureId().equals(feeStructureId)
                        && PaymentStatusName.ACTIVE.contains(payment.status()))
                .map(PaymentSnapshot::paymentId)
                .findFirst();
    }

    @Override
    public boolean updateStatus(Long paymentId, String expectedStatus, String newStatus) {
        AtomicBoolean updated = new AtomicBoolean();
        payments.computeIfPresent(paymentId, (id, payment) -> {
            if (!payment.status().equals(expectedStatus)) {
                return payment;
            }
            updated.set(true);
            return payment.withStatus(newStatus);
        });
        return updated.get();
    }

    public PaymentSnapshot createPending(Long studentId, Long feeStructureId) {
        PaymentSnapshot payment = new PaymentSnapshot(sequence.incrementAndGet(), studentId, feeStructureId,
                PaymentStatusName.PENDING);
        payments.put(payment.paymentId(), payment);
        return payment;
    }
}
