package com.rupp.payment.dto.response;

import com.rupp.payment.entity.ServiceFee;
import com.rupp.payment.entity.ServiceFeeCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A campus service fee. The "my*" fields are filled only by GET /service-fees/my and describe
 * the logged-in student's latest payment for it:
 * myStatus = NOT_PAID | PENDING | VERIFIED | PAID.
 */
public record ServiceFeeResponse(
        Long id,
        String code,
        String name,
        ServiceFeeCategory category,
        String description,
        BigDecimal amount,
        String validityLabel,
        LocalDate validFrom,
        LocalDate validUntil,
        boolean repeatable,
        boolean active,
        String myStatus,
        Long myPaymentId,
        String myReceiptNumber,
        LocalDateTime myPaidAt
) {
    public static ServiceFeeResponse from(ServiceFee fee) {
        return withStatus(fee, null, null, null, null);
    }

    public static ServiceFeeResponse withStatus(ServiceFee fee, String status, Long paymentId,
                                                String receiptNumber, LocalDateTime paidAt) {
        return new ServiceFeeResponse(
                fee.getId(), fee.getCode(), fee.getName(), fee.getCategory(), fee.getDescription(),
                fee.getAmount(), fee.getValidityLabel(), fee.getValidFrom(), fee.getValidUntil(),
                fee.isRepeatable(), fee.isActive(), status, paymentId, receiptNumber, paidAt
        );
    }
}
