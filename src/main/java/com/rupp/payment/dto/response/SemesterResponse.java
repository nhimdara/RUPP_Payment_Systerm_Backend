package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Semester;
import java.time.LocalDate;

public class SemesterResponse {

    private Long id;
    private String semesterName;
    private Integer semesterNumber;
    private AcademicYearResponse academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    public SemesterResponse() {
    }

    public SemesterResponse(Long id, String semesterName, Integer semesterNumber,
                            AcademicYearResponse academicYear, LocalDate startDate,
                            LocalDate endDate, Boolean isActive) {
        this.id = id;
        this.semesterName = semesterName;
        this.semesterNumber = semesterNumber;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public static SemesterResponse fromEntity(Semester entity) {
        if (entity == null) return null;
        return new SemesterResponse(
                entity.getId(),
                entity.getSemesterName(),
                entity.getSemesterNumber(),
                AcademicYearResponse.fromEntity(entity.getAcademicYear()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getIsActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AcademicYearResponse getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYearResponse academicYear) {
        this.academicYear = academicYear;
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
