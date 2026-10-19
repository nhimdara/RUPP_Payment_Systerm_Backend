package com.rupp.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentSecurityCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentSecurityCleanupJob.class);

    private final IdempotencyService idempotencyService;
    private final PaymentVerificationService verificationService;

    public PaymentSecurityCleanupJob(IdempotencyService idempotencyService, PaymentVerificationService verificationService) {
        this.idempotencyService = idempotencyService;
        this.verificationService = verificationService;
    }

    @Scheduled(cron = "${payment.security.cleanup-cron:0 0 * * * *}")
    public void purgeExpired() {
        int idempotencyRecords = idempotencyService.purgeExpired();
        int otpChallenges = verificationService.expireStaleChallenges();
        log.info("Payment security cleanup: removed {} expired idempotency records, expired {} stale OTP challenges",
                idempotencyRecords, otpChallenges);
    }
}
