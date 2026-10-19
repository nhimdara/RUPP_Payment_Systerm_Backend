package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends PaymentSecurityException {

    public UnauthenticatedException() {
        super(HttpStatus.UNAUTHORIZED, "Authentication is required");
    }
}
