package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class OtpChallengeStateException extends PaymentSecurityException {

    public OtpChallengeStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
