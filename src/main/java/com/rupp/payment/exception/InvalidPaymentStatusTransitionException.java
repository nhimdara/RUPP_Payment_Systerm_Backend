package com.rupp.payment.exception;

import com.rupp.payment.entity.PaymentStatus;

public class InvalidPaymentStatusTransitionException extends RuntimeException {

    private final PaymentStatus currentStatus;
    private final PaymentStatus targetStatus;

    public InvalidPaymentStatusTransitionException(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        super(String.format("Invalid payment status transition from '%s' to '%s'", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidPaymentStatusTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }

    public PaymentStatus getTargetStatus() {
        return targetStatus;
    }
}
