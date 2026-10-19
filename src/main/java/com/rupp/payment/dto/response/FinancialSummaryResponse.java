package com.rupp.payment.dto.response;

import java.math.BigDecimal;

public class FinancialSummaryResponse {

    private BigDecimal totalRevenueCollected;
    private BigDecimal totalPendingRevenue;
    private long totalConfirmedPayments;
    private long totalPendingPayments;
    private long totalRejectedPayments;
    private long totalStudents;
    private long totalStudentsPaid;

    public FinancialSummaryResponse() {
    }

    public FinancialSummaryResponse(BigDecimal totalRevenueCollected, BigDecimal totalPendingRevenue,
                                    long totalConfirmedPayments, long totalPendingPayments,
                                    long totalRejectedPayments, long totalStudents, long totalStudentsPaid) {
        this.totalRevenueCollected = totalRevenueCollected;
        this.totalPendingRevenue = totalPendingRevenue;
        this.totalConfirmedPayments = totalConfirmedPayments;
        this.totalPendingPayments = totalPendingPayments;
        this.totalRejectedPayments = totalRejectedPayments;
        this.totalStudents = totalStudents;
        this.totalStudentsPaid = totalStudentsPaid;
    }

    public BigDecimal getTotalRevenueCollected() {
        return totalRevenueCollected;
    }

    public void setTotalRevenueCollected(BigDecimal totalRevenueCollected) {
        this.totalRevenueCollected = totalRevenueCollected;
    }

    public BigDecimal getTotalPendingRevenue() {
        return totalPendingRevenue;
    }

    public void setTotalPendingRevenue(BigDecimal totalPendingRevenue) {
        this.totalPendingRevenue = totalPendingRevenue;
    }

    public long getTotalConfirmedPayments() {
        return totalConfirmedPayments;
    }

    public void setTotalConfirmedPayments(long totalConfirmedPayments) {
        this.totalConfirmedPayments = totalConfirmedPayments;
    }

    public long getTotalPendingPayments() {
        return totalPendingPayments;
    }

    public void setTotalPendingPayments(long totalPendingPayments) {
        this.totalPendingPayments = totalPendingPayments;
    }

    public long getTotalRejectedPayments() {
        return totalRejectedPayments;
    }

    public void setTotalRejectedPayments(long totalRejectedPayments) {
        this.totalRejectedPayments = totalRejectedPayments;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalStudentsPaid() {
        return totalStudentsPaid;
    }

    public void setTotalStudentsPaid(long totalStudentsPaid) {
        this.totalStudentsPaid = totalStudentsPaid;
    }
}
