package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicatePaymentException extends PaymentSecurityException {

    public DuplicatePaymentException(Long existingPaymentId) {
        super(HttpStatus.CONFLICT,
                "Student already has a pending, verified or confirmed payment for this tuition fee",
                Map.of("existingPaymentId", existingPaymentId));
    }
}
