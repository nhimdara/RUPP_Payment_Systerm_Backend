package com.rupp.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "payment.security")
public record PaymentSecurityProperties(@DefaultValue Otp otp, @DefaultValue Idempotency idempotency) {

    public record Otp(
            @DefaultValue("5m") Duration codeTtl,
            @DefaultValue("5") int maxAttempts,
            @DefaultValue("60s") Duration resendCooldown,
            @DefaultValue("15m") Duration lockoutDuration,
            @DefaultValue("") String hashSecret) {
    }

    public record Idempotency(@DefaultValue("24h") Duration ttl) {
    }
}
