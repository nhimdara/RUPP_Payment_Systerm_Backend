package com.rupp.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long id;

    @Column(name = "semester_name", nullable = false)
    private String semesterName;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    public Semester() {
    }

    public Semester(Long id, String semesterName, Integer semesterNumber, AcademicYear academicYear,
                    LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.id = id;
        this.semesterName = semesterName;
        this.semesterNumber = semesterNumber;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive != null ? isActive : false;
    }

    public Semester(String semesterName, Integer semesterNumber, AcademicYear academicYear,
                    LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.semesterName = semesterName;
        this.semesterNumber = semesterNumber;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive != null ? isActive : false;
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

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
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
        isActive = active != null ? active : false;
    }
}
