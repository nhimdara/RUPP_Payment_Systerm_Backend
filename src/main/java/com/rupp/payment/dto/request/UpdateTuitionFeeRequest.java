package com.rupp.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateTuitionFeeRequest {

    @Schema(description = "ID of the faculty", example = "1")
    private Long facultyId;

    @Schema(description = "ID of the academic year", example = "1")
    private Long academicYearId;

    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @Schema(description = "Total tuition fee amount", example = "650.00")
    private BigDecimal totalAmount;

    @Schema(description = "Payment due date", example = "2026-12-31")
    private LocalDate dueDate;

    public UpdateTuitionFeeRequest() {
    }

    public UpdateTuitionFeeRequest(Long facultyId, Long academicYearId, BigDecimal totalAmount, LocalDate dueDate) {
        this.facultyId = facultyId;
        this.academicYearId = academicYearId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
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
