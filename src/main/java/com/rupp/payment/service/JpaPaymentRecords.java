package com.rupp.payment.service;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.PaymentRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Primary
public class JpaPaymentRecords implements PaymentRecords {

    private final PaymentRepository paymentRepository;

    public JpaPaymentRecords(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentSnapshot> findById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findActivePaymentId(Long studentId, Long feeStructureId) {
        return paymentRepository.findByStudentIdAndFeeStructureId(studentId, feeStructureId)
                .stream()
                .filter(p -> p.getStatus() != null && PaymentStatusName.ACTIVE.contains(p.getStatus().name()))
                .map(Payment::getId)
                .findFirst();
    }

    @Override
    @Transactional
    public boolean updateStatus(Long paymentId, String expectedStatus, String newStatus) {
        Optional<Payment> opt = paymentRepository.findById(paymentId);
        if (opt.isEmpty()) {
            return false;
        }
        Payment payment = opt.get();
        if (payment.getStatus() == null || !payment.getStatus().name().equals(expectedStatus)) {
            return false;
        }
        payment.setStatus(PaymentStatus.valueOf(newStatus));
        paymentRepository.save(payment);
        return true;
    }

    private PaymentSnapshot toSnapshot(Payment payment) {
        Long studentId = payment.getStudent() != null ? payment.getStudent().getId() : null;
        Long feeStructureId = payment.getFeeStructure() != null ? payment.getFeeStructure().getId() : null;
        String status = payment.getStatus() != null ? payment.getStatus().name() : null;
        return new PaymentSnapshot(payment.getId(), studentId, feeStructureId, status);
    }
}
