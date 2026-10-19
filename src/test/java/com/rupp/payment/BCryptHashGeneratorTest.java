package com.rupp.payment;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BCryptHashGeneratorTest {

    @Test
    void verifyAndGenerateBcryptHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String adminHash = encoder.encode("admin123");
        String financeHash = encoder.encode("finance123");
        String studentHash = encoder.encode("student123");

        System.out.println("BCRYPT_ADMIN=" + adminHash);
        System.out.println("BCRYPT_FINANCE=" + financeHash);
        System.out.println("BCRYPT_STUDENT=" + studentHash);

        assertTrue(encoder.matches("admin123", adminHash));
        assertTrue(encoder.matches("finance123", financeHash));
        assertTrue(encoder.matches("student123", studentHash));
    }
}
