package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyKeyReuseException extends PaymentSecurityException {

    public IdempotencyKeyReuseException() {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "Idempotency-Key was already used with a different request");
    }
}
