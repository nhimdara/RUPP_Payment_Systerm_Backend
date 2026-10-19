package com.rupp.payment.dto.response;

import com.rupp.payment.entity.AcademicYear;
import java.time.LocalDate;

public class AcademicYearResponse {

    private Long id;
    private String yearLabel;
    private LocalDate startDate;
    private LocalDate endDate;

    public AcademicYearResponse() {
    }

    public AcademicYearResponse(Long id, String yearLabel, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.yearLabel = yearLabel;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static AcademicYearResponse fromEntity(AcademicYear entity) {
        if (entity == null) return null;
        return new AcademicYearResponse(
                entity.getId(),
                entity.getYearLabel(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYearLabel() {
        return yearLabel;
    }

    public void setYearLabel(String yearLabel) {
        this.yearLabel = yearLabel;
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
}
