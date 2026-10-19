package com.rupp.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyRevenueReport {

    private LocalDate date;
    private BigDecimal totalRevenue;
    private long transactionCount;

    public DailyRevenueReport() {
    }

    public DailyRevenueReport(LocalDate date, BigDecimal totalRevenue, long transactionCount) {
        this.date = date;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.transactionCount = transactionCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
