package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class OtpChallengeNotFoundException extends PaymentSecurityException {

    public OtpChallengeNotFoundException() {
        super(HttpStatus.NOT_FOUND, "OTP challenge not found");
    }
}
