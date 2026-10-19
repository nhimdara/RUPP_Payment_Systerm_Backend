package com.rupp.payment.service;

import com.rupp.payment.config.PaymentSecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

// HMAC-SHA256 with a server secret: a 6-digit code hashed with plain SHA-256 is trivially brute-forced
@Component
public class OtpCodeHasher {

    private static final Logger log = LoggerFactory.getLogger(OtpCodeHasher.class);
    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec key;

    public OtpCodeHasher(PaymentSecurityProperties properties) {
        String secret = properties.otp().hashSecret();
        if (secret == null || secret.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            this.key = new SecretKeySpec(random, ALGORITHM);
            log.warn("payment.security.otp.hash-secret is not set; using a random per-startup key (pending OTPs won't survive a restart).");
        } else {
            this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        }
    }

    public String hash(Long paymentId, Long userId, String code) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            byte[] digest = mac.doFinal((paymentId + ":" + userId + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }

    public boolean matches(String expectedHash, Long paymentId, Long userId, String code) {
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        byte[] actual = hash(paymentId, userId, code).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }
}
