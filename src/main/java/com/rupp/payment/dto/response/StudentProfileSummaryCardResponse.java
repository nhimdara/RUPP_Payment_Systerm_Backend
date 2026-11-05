package com.rupp.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * High-level presentation DTO for the Student Profile Summary Card.
 */
public class StudentProfileSummaryCardResponse {

    private Long studentId;
    private String studentIdCode;
    private String fullName;
    private String khmerName;
    private String initials;
    private String email;
    private String faculty;
    private String major;
    private String academicYear;
    private Integer year;
    private Integer semester;
    private String shift;
    private String enrollmentStatus;
    private BigDecimal totalTuitionFee;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private boolean fullyPaid;
    private String paymentStatus;
    private LocalDate dueDate;

    public StudentProfileSummaryCardResponse() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentIdCode() {
        return studentIdCode;
    }

    public void setStudentIdCode(String studentIdCode) {
        this.studentIdCode = studentIdCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getKhmerName() {
        return khmerName;
    }

    public void setKhmerName(String khmerName) {
        this.khmerName = khmerName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
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
}
