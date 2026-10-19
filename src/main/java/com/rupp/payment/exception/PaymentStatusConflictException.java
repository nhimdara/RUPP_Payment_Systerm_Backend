package com.rupp.payment.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentStatusConflictException extends PaymentSecurityException {

    public PaymentStatusConflictException(String currentStatus) {
        super(HttpStatus.CONFLICT, "Payment is " + currentStatus + "; this action is not allowed in that status",
                Map.of("currentStatus", currentStatus));
    }
}
