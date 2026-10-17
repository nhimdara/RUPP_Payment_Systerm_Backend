package com.rupp.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "academic_years")
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academic_year_id")
    private Long id;

    @Column(name = "year_label", nullable = false)
    private String yearLabel;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public AcademicYear() {
    }

    public AcademicYear(Long id, String yearLabel, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.yearLabel = yearLabel;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public AcademicYear(String yearLabel, LocalDate startDate, LocalDate endDate) {
        this.yearLabel = yearLabel;
        this.startDate = startDate;
        this.endDate = endDate;
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
