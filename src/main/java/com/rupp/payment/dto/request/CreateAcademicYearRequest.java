package com.rupp.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateAcademicYearRequest {

    @NotBlank(message = "Year label is required (e.g. '2024-2025')")
    private String yearLabel;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    public CreateAcademicYearRequest() {
    }

    public CreateAcademicYearRequest(String yearLabel, LocalDate startDate, LocalDate endDate) {
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
