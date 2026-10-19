package com.rupp.payment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long id;

    @Column(name = "semester_name", nullable = false, length = 100)
    private String semesterName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    public Semester() {
    }

    public Semester(String semesterName, AcademicYear academicYear) {
        this.semesterName = semesterName;
        this.academicYear = academicYear;
    }

    public Semester(Long id, String semesterName, AcademicYear academicYear) {
        this.id = id;
        this.semesterName = semesterName;
        this.academicYear = academicYear;
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

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }
}
