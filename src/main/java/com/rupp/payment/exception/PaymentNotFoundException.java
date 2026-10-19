package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends PaymentSecurityException {

    public PaymentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Payment not found");
    }
}
