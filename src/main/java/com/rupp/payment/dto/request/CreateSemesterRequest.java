package com.rupp.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateSemesterRequest {

    @NotBlank(message = "Semester name is required (e.g. 'Semester 1')")
    private String semesterName;

    @NotNull(message = "Semester number is required (e.g. 1, 2)")
    @Min(value = 1, message = "Semester number must be at least 1")
    private Integer semesterNumber;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive = false;

    public CreateSemesterRequest() {
    }

    public CreateSemesterRequest(String semesterName, Integer semesterNumber, Long academicYearId,
                                 LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.semesterName = semesterName;
        this.semesterNumber = semesterNumber;
        this.academicYearId = academicYearId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
