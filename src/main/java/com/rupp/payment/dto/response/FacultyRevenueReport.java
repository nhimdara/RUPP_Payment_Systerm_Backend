package com.rupp.payment.dto.response;

import java.math.BigDecimal;

public class FacultyRevenueReport {

    private Long facultyId;
    private String facultyName;
    private BigDecimal totalRevenue;
    private long totalPayments;

    public FacultyRevenueReport() {
    }

    public FacultyRevenueReport(Long facultyId, String facultyName, BigDecimal totalRevenue, long totalPayments) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.totalRevenue = totalRevenue;
        this.totalPayments = totalPayments;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}
