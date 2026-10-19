package com.rupp.payment.service;

import com.rupp.payment.entity.AuditAction;

import java.util.Objects;

// Actor is filled from CurrentUserProvider when not set explicitly
public record AuditEvent(AuditAction action, Long paymentId, String fromStatus, String toStatus,
                         String details, Long actorUserId, String actorRole) {

    public AuditEvent {
        Objects.requireNonNull(action, "action");
    }

    public static Builder of(AuditAction action) {
        return new Builder(action);
    }

    public static final class Builder {

        private final AuditAction action;
        private Long paymentId;
        private String fromStatus;
        private String toStatus;
        private String details;
        private Long actorUserId;
        private String actorRole;

        private Builder(AuditAction action) {
            this.action = action;
        }

        public Builder paymentId(Long paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder statusChange(String fromStatus, String toStatus) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder actor(Long actorUserId, String actorRole) {
            this.actorUserId = actorUserId;
            this.actorRole = actorRole;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(action, paymentId, fromStatus, toStatus, details, actorUserId, actorRole);
        }
    }
}
