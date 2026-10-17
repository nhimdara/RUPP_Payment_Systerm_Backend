package com.rupp.payment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculties")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_id")
    private Long id;

    @Column(name = "faculty_name", nullable = false)
    private String facultyName;

    @Column(name = "department")
    private String department;

    public Faculty() {
    }

    public Faculty(Long id, String facultyName, String department) {
        this.id = id;
        this.facultyName = facultyName;
        this.department = department;
    }

    public Faculty(String facultyName, String department) {
        this.facultyName = facultyName;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
