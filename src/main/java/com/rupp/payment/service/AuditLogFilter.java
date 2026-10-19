package com.rupp.payment.service;

import com.rupp.payment.entity.AuditAction;

import java.time.Instant;

public record AuditLogFilter(Long paymentId, AuditAction action, Long actorUserId, Instant from, Instant to) {

    public static AuditLogFilter forPayment(Long paymentId) {
        return new AuditLogFilter(paymentId, null, null, null, null);
    }
}
