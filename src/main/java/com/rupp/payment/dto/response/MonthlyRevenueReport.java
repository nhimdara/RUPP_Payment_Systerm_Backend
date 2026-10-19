package com.rupp.payment.dto.response;

import java.math.BigDecimal;

public class MonthlyRevenueReport {

    private String yearMonth;
    private int year;
    private int month;
    private String monthName;
    private BigDecimal totalRevenue;
    private long transactionCount;

    public MonthlyRevenueReport() {
    }

    public MonthlyRevenueReport(String yearMonth, int year, int month, String monthName, BigDecimal totalRevenue, long transactionCount) {
        this.yearMonth = yearMonth;
        this.year = year;
        this.month = month;
        this.monthName = monthName;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.transactionCount = transactionCount;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
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
