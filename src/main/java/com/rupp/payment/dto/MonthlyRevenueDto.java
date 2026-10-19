package com.rupp.payment.dto;

import java.math.BigDecimal;

public record MonthlyRevenueDto(int month, String monthName, BigDecimal total) {
}
