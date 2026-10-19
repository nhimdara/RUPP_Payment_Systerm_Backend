package com.rupp.payment.dto.response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FinancialSummaryResponse {

    private BigDecimal totalRevenue;
    private BigDecimal totalPendingAmount;
    private long totalTransactions;
    private long confirmedCount;
    private long pendingCount;
    private long verifiedCount;
    private long rejectedCount;
    private long refundedCount;
    private Map<String, FacultyStat> facultyBreakdown = new HashMap<>();
    private Map<String, MethodStat> methodBreakdown = new HashMap<>();

    public FinancialSummaryResponse() {
        this.totalRevenue = BigDecimal.ZERO;
        this.totalPendingAmount = BigDecimal.ZERO;
    }

    public static class FacultyStat {
        private long count;
        private BigDecimal totalAmount;

        public FacultyStat() {
        }

        public FacultyStat(long count, BigDecimal totalAmount) {
            this.count = count;
            this.totalAmount = totalAmount;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public static class MethodStat {
        private long count;
        private BigDecimal totalAmount;

        public MethodStat() {
        }

        public MethodStat(long count, BigDecimal totalAmount) {
            this.count = count;
            this.totalAmount = totalAmount;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalPendingAmount() {
        return totalPendingAmount;
    }

    public void setTotalPendingAmount(BigDecimal totalPendingAmount) {
        this.totalPendingAmount = totalPendingAmount;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(long confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getVerifiedCount() {
        return verifiedCount;
    }

    public void setVerifiedCount(long verifiedCount) {
        this.verifiedCount = verifiedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getRefundedCount() {
        return refundedCount;
    }

    public void setRefundedCount(long refundedCount) {
        this.refundedCount = refundedCount;
    }

    public Map<String, FacultyStat> getFacultyBreakdown() {
        return facultyBreakdown;
    }

    public void setFacultyBreakdown(Map<String, FacultyStat> facultyBreakdown) {
        this.facultyBreakdown = facultyBreakdown;
    }

    public Map<String, MethodStat> getMethodBreakdown() {
        return methodBreakdown;
    }

    public void setMethodBreakdown(Map<String, MethodStat> methodBreakdown) {
        this.methodBreakdown = methodBreakdown;
    }
}
