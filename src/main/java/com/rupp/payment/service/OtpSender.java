package com.rupp.payment.service;

import java.time.Instant;

// Delivery channel for OTP codes (SMS / email provider later)
public interface OtpSender {

    void send(Long userId, Long paymentId, String code, Instant expiresAt);
}
