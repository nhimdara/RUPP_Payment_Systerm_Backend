package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.TuitionFeeStructure;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TuitionFeeResponse {

    private Long id;
    private Faculty faculty;
    private AcademicYearResponse academicYear;
    private BigDecimal totalAmount;
    private LocalDate dueDate;

    public TuitionFeeResponse() {
    }

    public TuitionFeeResponse(Long id, Faculty faculty, AcademicYearResponse academicYear, BigDecimal totalAmount, LocalDate dueDate) {
        this.id = id;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public static TuitionFeeResponse fromEntity(TuitionFeeStructure fee) {
        if (fee == null) return null;
        return new TuitionFeeResponse(
                fee.getId(),
                fee.getFaculty(),
                fee.getAcademicYear() != null ? AcademicYearResponse.fromEntity(fee.getAcademicYear()) : null,
                fee.getTotalAmount(),
                fee.getDueDate()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public AcademicYearResponse getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYearResponse academicYear) {
        this.academicYear = academicYear;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
