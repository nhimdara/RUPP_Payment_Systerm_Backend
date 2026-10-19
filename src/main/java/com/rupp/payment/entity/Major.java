package com.rupp.payment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "majors", indexes = {
        @Index(name = "idx_majors_faculty", columnList = "faculty_id")
})
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Long id;

    @Column(name = "major_name", nullable = false)
    private String majorName;

    @Column(name = "major_code", nullable = false, unique = true, length = 50)
    private String majorCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(name = "description", length = 500)
    private String description;

    public Major() {
    }

    public Major(Long id, String majorName, String majorCode, Faculty faculty, String description) {
        this.id = id;
        this.majorName = majorName;
        this.majorCode = majorCode;
        this.faculty = faculty;
        this.description = description;
    }

    public Major(String majorName, String majorCode, Faculty faculty, String description) {
        this.majorName = majorName;
        this.majorCode = majorCode;
        this.faculty = faculty;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
