package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentAccessDeniedException extends PaymentSecurityException {

    public PaymentAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
