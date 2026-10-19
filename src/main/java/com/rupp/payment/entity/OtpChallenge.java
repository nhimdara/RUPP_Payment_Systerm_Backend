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

import java.time.Instant;
import java.util.UUID;

// Stores only an HMAC of the OTP code. payment_id refers to the ERD's Payment.Payment_ID
@Entity
@Table(name = "otp_challenge", indexes = {
        @Index(name = "idx_otp_challenge_payment_user", columnList = "payment_id, user_id"),
        @Index(name = "idx_otp_challenge_expires_at", columnList = "expires_at")
})
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false, updatable = false)
    private Long paymentId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    // Read-only association: gives user_id a real FK to users while writes go through userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_otp_challenge_user"))
    private User user;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OtpStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    protected OtpChallenge() {
    }

    public OtpChallenge(Long paymentId, Long userId, String codeHash, Instant now, Instant expiresAt) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.codeHash = codeHash;
        this.status = OtpStatus.PENDING;
        this.attempts = 0;
        this.createdAt = now;
        this.lastSentAt = now;
        this.expiresAt = expiresAt;
    }

    public boolean isCodeExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public int registerFailedAttempt() {
        return ++attempts;
    }

    public void lock(Instant now) {
        status = OtpStatus.LOCKED;
        lockedAt = now;
    }

    public void expire() {
        status = OtpStatus.EXPIRED;
    }

    public void resend(String newCodeHash, Instant now, Instant newExpiresAt) {
        codeHash = newCodeHash;
        lastSentAt = now;
        expiresAt = newExpiresAt;
    }

    public void markVerified(Instant now) {
        status = OtpStatus.VERIFIED;
        verifiedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}
