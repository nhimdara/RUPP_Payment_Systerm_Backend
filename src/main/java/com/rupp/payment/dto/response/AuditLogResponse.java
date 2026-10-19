package com.rupp.payment.dto.response;

import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.PaymentAuditLog;

import java.time.Instant;

public record AuditLogResponse(Long id, Long paymentId, AuditAction action, String fromStatus, String toStatus,
                               Long actorUserId, String actorRole, String ipAddress, String userAgent,
                               String details, Instant createdAt) {

    public static AuditLogResponse from(PaymentAuditLog log) {
        return new AuditLogResponse(log.getId(), log.getPaymentId(), log.getAction(), log.getFromStatus(),
                log.getToStatus(), log.getActorUserId(), log.getActorRole(), log.getIpAddress(),
                log.getUserAgent(), log.getDetails(), log.getCreatedAt());
    }
}
