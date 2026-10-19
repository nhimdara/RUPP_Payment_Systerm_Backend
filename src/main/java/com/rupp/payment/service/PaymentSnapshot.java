package com.rupp.payment.service;

// The Payment fields payment security needs (ERD: Payment_ID, Student_ID, Fee_Structure_ID, Payment_Status.Status_Name)
public record PaymentSnapshot(Long paymentId, Long studentId, Long feeStructureId, String status) {

    public PaymentSnapshot withStatus(String newStatus) {
        return new PaymentSnapshot(paymentId, studentId, feeStructureId, newStatus);
    }
}
