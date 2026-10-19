package com.rupp.payment.service;

import java.util.Set;

// Payment_Status.Status_Name values this module relies on; the Payment module should seed these rows
public final class PaymentStatusName {

    // Created by the student (Make Payment), waiting for OTP
    public static final String PENDING = "PENDING";
    // Student passed the OTP step (Make Payment <<include>> Verify Payment)
    public static final String VERIFIED = "VERIFIED";
    // Finance staff accepted it (Confirm Payment)
    public static final String CONFIRMED = "CONFIRMED";
    public static final String REJECTED = "REJECTED";

    // A student may hold at most one payment in these statuses per fee structure
    public static final Set<String> ACTIVE = Set.of(PENDING, VERIFIED, CONFIRMED);

    private PaymentStatusName() {
    }
}
