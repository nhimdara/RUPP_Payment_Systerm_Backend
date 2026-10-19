package com.rupp.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record DevPaymentResponse(Long paymentId, Long studentId, Long feeStructureId, String status,
                                 String method, String transactionRef, BigDecimal amount, Instant createdAt) {
}
