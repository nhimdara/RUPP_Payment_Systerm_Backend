package com.rupp.payment.service;

import com.rupp.payment.config.PaymentSecurityProperties;
import com.rupp.payment.dto.response.OtpChallengeResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.OtpChallenge;
import com.rupp.payment.entity.OtpStatus;
import com.rupp.payment.exception.OtpChallengeNotFoundException;
import com.rupp.payment.exception.OtpChallengeStateException;
import com.rupp.payment.exception.OtpExpiredException;
import com.rupp.payment.exception.OtpInvalidCodeException;
import com.rupp.payment.exception.OtpLockedException;
import com.rupp.payment.exception.OtpResendCooldownException;
import com.rupp.payment.exception.PaymentSecurityException;
import com.rupp.payment.exception.PaymentStatusConflictException;
import com.rupp.payment.repository.OtpChallengeRepository;
import com.rupp.payment.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

// Student side of "Make Payment <<include>> Verify Payment": a correct OTP moves the payment PENDING -> VERIFIED
@Service
public class PaymentVerificationService {

    private static final Set<OtpStatus> SUPERSEDABLE = EnumSet.of(OtpStatus.PENDING);

    private final OtpChallengeRepository repository;
    private final OtpCodeHasher hasher;
    private final OtpSender otpSender;
    private final PaymentOwnership ownership;
    private final PaymentRecords paymentRecords;
    private final PaymentAuditService auditService;
    private final CurrentUserProvider currentUser;
    private final Clock clock;
    private final PaymentSecurityProperties.Otp config;
    private final SecureRandom random = new SecureRandom();

    public PaymentVerificationService(OtpChallengeRepository repository, OtpCodeHasher hasher, OtpSender otpSender,
                                      PaymentOwnership ownership, PaymentRecords paymentRecords,
                                      PaymentAuditService auditService, CurrentUserProvider currentUser,
                                      Clock clock, PaymentSecurityProperties properties) {
        this.repository = repository;
        this.hasher = hasher;
        this.otpSender = otpSender;
        this.ownership = ownership;
        this.paymentRecords = paymentRecords;
        this.auditService = auditService;
        this.currentUser = currentUser;
        this.clock = clock;
        this.config = properties.otp();
    }

    // Request

    @Transactional(noRollbackFor = PaymentSecurityException.class)
    public OtpChallengeResponse requestOtp(Long paymentId) {
        Long userId = currentUser.getUserId();
        Instant now = clock.instant();
        PaymentSnapshot payment = ownership.requireOwnedPayment(paymentId);
        if (!PaymentStatusName.PENDING.equals(payment.status())) {
            throw new PaymentStatusConflictException(payment.status());
        }

        if (repository.existsByPaymentIdAndUserIdAndStatusAndLockedAtAfter(
                paymentId, userId, OtpStatus.LOCKED, now.minus(config.lockoutDuration()))) {
            throw new OtpLockedException();
        }
        repository.findFirstByPaymentIdAndUserIdOrderByLastSentAtDesc(paymentId, userId)
                .ifPresent(last -> enforceCooldown(last, now));

        repository.findByPaymentIdAndUserIdAndStatusIn(paymentId, userId, SUPERSEDABLE)
                .forEach(OtpChallenge::expire);

        String code = generateCode();
        OtpChallenge challenge = repository.save(new OtpChallenge(paymentId, userId,
                hasher.hash(paymentId, userId, code), now, now.plus(config.codeTtl())));
        otpSender.send(userId, paymentId, code, challenge.getExpiresAt());
        auditService.record(AuditAction.OTP_REQUESTED, paymentId, "challengeId=" + challenge.getId());
        return new OtpChallengeResponse(challenge.getId(), paymentId, challenge.getExpiresAt());
    }

    // Resend

    @Transactional(noRollbackFor = PaymentSecurityException.class)
    public OtpChallengeResponse resendOtp(UUID challengeId) {
        Long userId = currentUser.getUserId();
        Instant now = clock.instant();
        OtpChallenge challenge = loadOwned(challengeId, userId);

        switch (challenge.getStatus()) {
            case LOCKED -> throw new OtpLockedException();
            case VERIFIED -> throw new OtpChallengeStateException("OTP challenge is already verified");
            case EXPIRED -> throw new OtpChallengeStateException("OTP challenge is no longer active; request a new code");
            case PENDING -> enforceCooldown(challenge, now);
        }

        String code = generateCode();
        challenge.resend(hasher.hash(challenge.getPaymentId(), userId, code), now, now.plus(config.codeTtl()));
        otpSender.send(userId, challenge.getPaymentId(), code, challenge.getExpiresAt());
        auditService.record(AuditAction.OTP_RESENT, challenge.getPaymentId(), "challengeId=" + challengeId);
        return new OtpChallengeResponse(challenge.getId(), challenge.getPaymentId(), challenge.getExpiresAt());
    }

    // Verify (attempt counters and lockout must persist even though an exception is thrown)

    @Transactional(noRollbackFor = PaymentSecurityException.class)
    public PaymentStatusResponse verifyOtp(UUID challengeId, String code) {
        Long userId = currentUser.getUserId();
        Instant now = clock.instant();
        OtpChallenge challenge = loadOwned(challengeId, userId);
        Long paymentId = challenge.getPaymentId();

        switch (challenge.getStatus()) {
            case LOCKED -> throw new OtpLockedException();
            case VERIFIED -> throw new OtpChallengeStateException("OTP challenge is already verified");
            case EXPIRED -> throw new OtpExpiredException();
            case PENDING -> {
            }
        }
        if (challenge.isCodeExpired(now)) {
            challenge.expire();
            auditService.record(AuditAction.OTP_EXPIRED, paymentId, "challengeId=" + challengeId);
            throw new OtpExpiredException();
        }
        if (!hasher.matches(challenge.getCodeHash(), paymentId, userId, code)) {
            int attempts = challenge.registerFailedAttempt();
            auditService.record(AuditAction.OTP_FAILED, paymentId,
                    "challengeId=%s attempt=%d/%d".formatted(challengeId, attempts, config.maxAttempts()));
            if (attempts >= config.maxAttempts()) {
                challenge.lock(now);
                auditService.record(AuditAction.OTP_LOCKED, paymentId, "challengeId=" + challengeId);
                throw new OtpLockedException();
            }
            throw new OtpInvalidCodeException(config.maxAttempts() - attempts);
        }

        if (!paymentRecords.updateStatus(paymentId, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED)) {
            challenge.expire();
            throw new PaymentStatusConflictException(currentStatus(paymentId));
        }
        challenge.markVerified(now);
        auditService.record(AuditAction.OTP_VERIFIED, paymentId, "challengeId=" + challengeId);
        auditService.recordStatusChange(paymentId, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED,
                "Verified by student OTP");
        return new PaymentStatusResponse(paymentId, PaymentStatusName.VERIFIED);
    }

    @Transactional
    public int expireStaleChallenges() {
        return repository.expireStale(OtpStatus.PENDING, OtpStatus.EXPIRED, clock.instant());
    }

    private OtpChallenge loadOwned(UUID challengeId, Long userId) {
        return repository.findForUpdate(challengeId)
                .filter(challenge -> challenge.getUserId().equals(userId))
                .orElseThrow(OtpChallengeNotFoundException::new);
    }

    private String currentStatus(Long paymentId) {
        return paymentRecords.findById(paymentId).map(PaymentSnapshot::status).orElse("missing");
    }

    private void enforceCooldown(OtpChallenge challenge, Instant now) {
        Instant allowedAt = challenge.getLastSentAt().plus(config.resendCooldown());
        if (now.isBefore(allowedAt)) {
            long millis = Duration.between(now, allowedAt).toMillis();
            throw new OtpResendCooldownException((millis + 999) / 1000);
        }
    }

    private String generateCode() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }
}
