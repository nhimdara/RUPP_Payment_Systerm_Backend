package com.rupp.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TuitionCalculationResponse {

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String facultyName;
    private String academicYear;
    private String majorName;
    private BigDecimal totalTuitionFee;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private boolean fullyPaid;
    private String paymentStatus;
    private LocalDate dueDate;
    private List<PaymentResponse> paymentHistory;

    public TuitionCalculationResponse() {
    }

    public TuitionCalculationResponse(Long studentId, String studentName, String studentEmail,
                                      String facultyName, String academicYear, String majorName,
                                      BigDecimal totalTuitionFee, BigDecimal totalPaid, BigDecimal remainingBalance,
                                      boolean fullyPaid, String paymentStatus, LocalDate dueDate,
                                      List<PaymentResponse> paymentHistory) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.facultyName = facultyName;
        this.academicYear = academicYear;
        this.majorName = majorName;
        this.totalTuitionFee = totalTuitionFee;
        this.totalPaid = totalPaid;
        this.remainingBalance = remainingBalance;
        this.fullyPaid = fullyPaid;
        this.paymentStatus = paymentStatus;
        this.dueDate = dueDate;
        this.paymentHistory = paymentHistory;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public BigDecimal getTotalTuitionFee() {
        return totalTuitionFee;
    }

    public void setTotalTuitionFee(BigDecimal totalTuitionFee) {
        this.totalTuitionFee = totalTuitionFee;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public boolean isFullyPaid() {
        return fullyPaid;
    }

    public void setFullyPaid(boolean fullyPaid) {
        this.fullyPaid = fullyPaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<PaymentResponse> getPaymentHistory() {
        return paymentHistory;
    }

    public void setPaymentHistory(List<PaymentResponse> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }
}
