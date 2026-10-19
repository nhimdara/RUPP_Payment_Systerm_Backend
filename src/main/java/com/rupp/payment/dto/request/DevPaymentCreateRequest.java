package com.rupp.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Mirrors the ERD: Payment.Fee_Structure_ID plus Payment_Detail.Method / Transaction_Ref / Amount
public record DevPaymentCreateRequest(
        @NotNull @Positive Long feeStructureId,
        @NotBlank @Size(max = 30) String method,
        @NotBlank @Size(max = 100) String transactionRef,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {
}
