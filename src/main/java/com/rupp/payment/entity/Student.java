package com.rupp.payment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "major_id")
    private Major major;

    public Student() {
    }

    public Student(Long id, User user, String firstName, String lastName, Faculty faculty, AcademicYear academicYear) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.faculty = faculty;
        this.academicYear = academicYear;
    }

    public Student(Long id, User user, String firstName, String lastName, Faculty faculty, AcademicYear academicYear, Major major) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.major = major;
    }

    public Student(User user, String firstName, String lastName, Faculty faculty, AcademicYear academicYear) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.faculty = faculty;
        this.academicYear = academicYear;
    }

    public Student(User user, String firstName, String lastName, Faculty faculty, AcademicYear academicYear, Major major) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.major = major;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }
}
