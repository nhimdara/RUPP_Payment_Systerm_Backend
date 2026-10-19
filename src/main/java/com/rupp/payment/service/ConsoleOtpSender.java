package com.rupp.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Dev only: prints the OTP to the application log
@Component
public class ConsoleOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleOtpSender.class);

    public ConsoleOtpSender() {
        log.warn("ConsoleOtpSender is active: OTP codes are written to the log. Replace with an SMS/email sender before production.");
    }

    @Override
    public void send(Long userId, Long paymentId, String code, Instant expiresAt) {
        log.info("[DEV OTP] userId={} paymentId={} code={} expiresAt={}", userId, paymentId, code, expiresAt);
    }
}
