package com.rupp.payment.service;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.entity.Receipt;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.ReceiptRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Primary
public class JpaPaymentRecords implements PaymentRecords {

    private final PaymentRepository paymentRepository;

    private final ReceiptRepository receiptRepository;

    public JpaPaymentRecords(PaymentRepository paymentRepository, ReceiptRepository receiptRepository) {
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
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
        // Confirmed payments always get an official receipt, whichever endpoint confirmed them
        if (payment.getStatus() == PaymentStatus.CONFIRMED && payment.getReceipt() == null
                && receiptRepository.findByPaymentId(payment.getId()).isEmpty()) {
            Receipt receipt = new Receipt(payment, PaymentService.generateReceiptNumber(payment.getId()),
                    LocalDateTime.now(), payment.getTotalPaid());
            payment.setReceipt(receipt);
            receiptRepository.save(receipt);
        }
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
