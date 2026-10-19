package com.rupp.payment.entity;

import jakarta.persistence.*;

/** TEMPORARY stub. Replace with Dara's real Student entity when merged. */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentCode;

    @Column(nullable = false)
    private String fullName;

    public Student() {
    }

    public Student(String studentCode, String fullName) {
        this.studentCode = studentCode;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
