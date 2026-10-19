package com.rupp.payment.service;

import com.rupp.payment.entity.Student;
import com.rupp.payment.exception.PaymentAccessDeniedException;
import com.rupp.payment.exception.PaymentNotFoundException;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

// ERD: User 1-1 Student 1-N Payment. A student may only act on their own payments
@Service
public class PaymentOwnership {

    private final PaymentRecords paymentRecords;
    private final StudentRepository studentRepository;
    private final CurrentUserProvider currentUser;

    public PaymentOwnership(PaymentRecords paymentRecords, StudentRepository studentRepository,
                            CurrentUserProvider currentUser) {
        this.paymentRecords = paymentRecords;
        this.studentRepository = studentRepository;
        this.currentUser = currentUser;
    }

    public Long currentStudentId() {
        return studentRepository.findByUserId(currentUser.getUserId())
                .map(Student::getId)
                .orElseThrow(() -> new PaymentAccessDeniedException("No student profile is linked to this account"));
    }

    // Another student's payment is reported as not found so payment ids can't be probed
    public PaymentSnapshot requireOwnedPayment(Long paymentId) {
        Long studentId = currentStudentId();
        return paymentRecords.findById(paymentId)
                .filter(payment -> payment.studentId().equals(studentId))
                .orElseThrow(PaymentNotFoundException::new);
    }
}
