package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class OtpLockedException extends PaymentSecurityException {

    public OtpLockedException() {
        super(HttpStatus.LOCKED, "Too many incorrect OTP attempts; verification is locked for this payment");
    }
}
