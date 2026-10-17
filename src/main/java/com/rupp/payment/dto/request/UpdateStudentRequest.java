package com.rupp.payment.dto.request;

public class UpdateStudentRequest {

    private String firstName;
    private String lastName;
    private Long facultyId;
    private Long academicYearId;

    public UpdateStudentRequest() {
    }

    public UpdateStudentRequest(String firstName, String lastName, Long facultyId, Long academicYearId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.facultyId = facultyId;
        this.academicYearId = academicYearId;
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
}
