package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OtpResendCooldownException extends PaymentSecurityException {

    private final long retryAfterSeconds;

    public OtpResendCooldownException(long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another OTP code",
                Map.of("retryAfterSeconds", retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
