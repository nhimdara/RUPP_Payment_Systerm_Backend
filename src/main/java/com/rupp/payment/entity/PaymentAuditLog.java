package com.rupp.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

// Append-only: no setters, nothing updatable. from/to_status hold Payment_Status.Status_Name values
@Entity
@Immutable
@Table(name = "payment_audit_log", indexes = {
        @Index(name = "idx_payment_audit_log_payment_created", columnList = "payment_id, created_at"),
        @Index(name = "idx_payment_audit_log_created_at", columnList = "created_at")
})
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", updatable = false)
    private Long paymentId;

    // Plain varchar holding AuditAction.name(): a mapped enum would get a CHECK constraint that ddl-auto=update never widens
    @Column(name = "action", nullable = false, updatable = false, length = 40)
    private String action;

    @Column(name = "from_status", updatable = false, length = 40)
    private String fromStatus;

    @Column(name = "to_status", updatable = false, length = 40)
    private String toStatus;

    @Column(name = "actor_user_id", updatable = false)
    private Long actorUserId;

    // Read-only association: gives actor_user_id a real FK to users while writes go through actorUserId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", referencedColumnName = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_payment_audit_log_actor"))
    private User actor;

    @Column(name = "actor_role", updatable = false, length = 40)
    private String actorRole;

    @Column(name = "ip_address", updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", updatable = false, length = 512)
    private String userAgent;

    @Column(name = "details", updatable = false, columnDefinition = "text")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentAuditLog() {
    }

    public PaymentAuditLog(Long paymentId, AuditAction action, String fromStatus, String toStatus,
                           Long actorUserId, String actorRole, String ipAddress, String userAgent,
                           String details, Instant createdAt) {
        this.paymentId = paymentId;
        this.action = action.name();
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorUserId = actorUserId;
        this.actorRole = actorRole;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public AuditAction getAction() {
        return AuditAction.valueOf(action);
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
