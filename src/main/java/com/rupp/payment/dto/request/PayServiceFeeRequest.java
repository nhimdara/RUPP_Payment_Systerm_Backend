package com.rupp.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Student pays a campus service fee. The amount always comes from the fee itself. */
public record PayServiceFeeRequest(
        @NotBlank(message = "Payment method is required (e.g., KHQR_BAKONG, ABA_PAYWAY, WING)")
        String paymentMethod,
        @NotBlank(message = "Transaction reference is required")
        @Size(max = 100, message = "Transaction reference is too long")
        String transactionRef,
        @Size(max = 500, message = "Remarks must be at most 500 characters")
        String remarks
) {
}
