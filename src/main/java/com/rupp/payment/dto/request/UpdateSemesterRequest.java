package com.rupp.payment.dto.request;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public class UpdateSemesterRequest {

    private String semesterName;

    @Min(value = 1, message = "Semester number must be at least 1")
    private Integer semesterNumber;

    private Long academicYearId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    public UpdateSemesterRequest() {
    }

    public UpdateSemesterRequest(String semesterName, Integer semesterNumber, Long academicYearId,
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
