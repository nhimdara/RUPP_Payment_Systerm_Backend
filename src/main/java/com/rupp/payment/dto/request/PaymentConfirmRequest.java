package com.rupp.payment.dto.request;

import jakarta.validation.constraints.Size;

public record PaymentConfirmRequest(@Size(max = 500) String note) {
}
