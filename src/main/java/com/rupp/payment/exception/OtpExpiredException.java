package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends PaymentSecurityException {

    public OtpExpiredException() {
        super(HttpStatus.GONE, "OTP code has expired; request a new code");
    }
}
