package com.rupp.payment.dto.request;

import java.time.LocalDate;

public class UpdateAcademicYearRequest {

    private String yearLabel;
    private LocalDate startDate;
    private LocalDate endDate;

    public UpdateAcademicYearRequest() {
    }

    public UpdateAcademicYearRequest(String yearLabel, LocalDate startDate, LocalDate endDate) {
        this.yearLabel = yearLabel;
        this.startDate = startDate;
        this.endDate = endDate;
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
