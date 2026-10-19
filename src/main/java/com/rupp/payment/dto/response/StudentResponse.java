package com.rupp.payment.dto.response;

import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Student;

public class StudentResponse {
    private Long id;
    private UserResponse user;
    private String firstName;
    private String lastName;
    private Faculty faculty;
    private AcademicYear academicYear;
    private MajorResponse major;

    public StudentResponse() {
    }

    public StudentResponse(Long id, UserResponse user, String firstName, String lastName, Faculty faculty, AcademicYear academicYear, MajorResponse major) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.major = major;
    }

    public static StudentResponse fromEntity(Student student) {
        if (student == null) return null;
        return new StudentResponse(
                student.getId(),
                UserResponse.fromEntity(student.getUser()),
                student.getFirstName(),
                student.getLastName(),
                student.getFaculty(),
                student.getAcademicYear(),
                MajorResponse.fromEntity(student.getMajor())
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
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

    public MajorResponse getMajor() {
        return major;
    }

    public void setMajor(MajorResponse major) {
        this.major = major;
    }
}
