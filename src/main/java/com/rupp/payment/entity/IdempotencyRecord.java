package com.rupp.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "idempotency_record",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_record_user_key", columnNames = {"user_id", "idempotency_key"}),
        indexes = @Index(name = "idx_idempotency_record_expires_at", columnList = "expires_at"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Read-only association: gives user_id a real FK to users while writes go through userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_idempotency_record_user"))
    private User user;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_content_type")
    private String responseContentType;

    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyRecord() {
    }

    public static IdempotencyRecord start(Long userId, String key, String requestHash, Instant now, Instant expiresAt) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.userId = userId;
        record.idempotencyKey = key;
        record.requestHash = requestHash;
        record.status = IdempotencyStatus.IN_PROGRESS;
        record.createdAt = now;
        record.expiresAt = expiresAt;
        return record;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
