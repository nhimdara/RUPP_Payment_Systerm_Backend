package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyRequestInProgressException extends PaymentSecurityException {

    public IdempotencyRequestInProgressException() {
        super(HttpStatus.CONFLICT, "A request with this Idempotency-Key is still being processed");
    }
}
