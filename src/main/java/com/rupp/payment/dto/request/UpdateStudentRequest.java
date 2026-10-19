package com.rupp.payment.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateStudentRequest {

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
    private Long facultyId;
    private Long academicYearId;
    private Long majorId;

    public UpdateStudentRequest() {
    }

    public UpdateStudentRequest(String firstName, String lastName, Long facultyId, Long academicYearId, Long majorId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.facultyId = facultyId;
        this.academicYearId = academicYearId;
        this.majorId = majorId;
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

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }
}
