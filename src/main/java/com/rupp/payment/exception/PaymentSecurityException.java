package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class PaymentSecurityException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> details;

    protected PaymentSecurityException(HttpStatus status, String message) {
        this(status, message, Map.of());
    }

    protected PaymentSecurityException(HttpStatus status, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
