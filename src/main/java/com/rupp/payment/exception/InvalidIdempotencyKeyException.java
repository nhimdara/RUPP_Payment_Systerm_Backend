package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class InvalidIdempotencyKeyException extends PaymentSecurityException {

    private InvalidIdempotencyKeyException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static InvalidIdempotencyKeyException missing() {
        return new InvalidIdempotencyKeyException("Idempotency-Key header is required");
    }

    public static InvalidIdempotencyKeyException malformed() {
        return new InvalidIdempotencyKeyException("Idempotency-Key header must be a valid UUID");
    }
}
