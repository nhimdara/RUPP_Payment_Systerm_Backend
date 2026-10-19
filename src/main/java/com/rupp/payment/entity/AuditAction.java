package com.rupp.payment.entity;

public enum AuditAction {

    PAYMENT_CREATED(false),
    STATUS_CHANGED(false),
    OTP_REQUESTED(false),
    OTP_RESENT(false),
    OTP_VERIFIED(false),
    OTP_FAILED(true),
    OTP_EXPIRED(true),
    OTP_LOCKED(true),
    DUPLICATE_BLOCKED(true),
    IDEMPOTENT_REPLAY(true),
    IDEMPOTENCY_KEY_REUSED(true),
    IDEMPOTENCY_CONFLICT(true);

    // Independent events are committed in their own transaction so a rollback can't erase them
    private final boolean independent;

    AuditAction(boolean independent) {
        this.independent = independent;
    }

    public boolean isIndependent() {
        return independent;
    }
}
