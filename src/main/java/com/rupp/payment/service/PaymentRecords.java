package com.rupp.payment.service;

import java.util.Optional;

// Port to the Payment module. Implement it with the Payment JPA repository, then delete InMemoryPaymentRecords
public interface PaymentRecords {

    Optional<PaymentSnapshot> findById(Long paymentId);

    // Id of the student's payment in an ACTIVE status for this fee structure, if any
    Optional<Long> findActivePaymentId(Long studentId, Long feeStructureId);

    // Compare-and-set, e.g. "update ... set status = :new where id = :id and status = :expected"; false if the status had moved on
    boolean updateStatus(Long paymentId, String expectedStatus, String newStatus);
}
