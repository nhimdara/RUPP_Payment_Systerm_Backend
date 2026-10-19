package com.rupp.payment.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        BigDecimal todayRevenue,
        BigDecimal thisMonthRevenue,
        BigDecimal thisYearRevenue,
        long completedCount,
        long pendingCount,
        long failedCount) {
}
