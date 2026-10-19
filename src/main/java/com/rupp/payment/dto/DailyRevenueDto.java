package com.rupp.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueDto(LocalDate date, BigDecimal total) {
}
