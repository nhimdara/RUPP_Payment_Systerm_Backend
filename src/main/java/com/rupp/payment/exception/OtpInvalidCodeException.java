package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OtpInvalidCodeException extends PaymentSecurityException {

    public OtpInvalidCodeException(int remainingAttempts) {
        super(HttpStatus.BAD_REQUEST, "Incorrect OTP code", Map.of("remainingAttempts", remainingAttempts));
    }
}
