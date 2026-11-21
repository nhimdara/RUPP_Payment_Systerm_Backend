package com.rupp.payment.dto.request;

import com.rupp.payment.entity.ServiceFeeCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Admin creates or replaces a campus service fee. */
public record ServiceFeeRequest(
        @NotBlank(message = "Code is required") @Size(max = 40) String code,
        @NotBlank(message = "Name is required") @Size(max = 150) String name,
        @NotNull(message = "Category is required") ServiceFeeCategory category,
        @Size(max = 500) String description,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        @Size(max = 100) String validityLabel,
        LocalDate validFrom,
        LocalDate validUntil,
        Boolean repeatable
) {
}
