package com.rupp.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateFacultyRequest {

    @NotBlank(message = "Faculty name is required")
    @Size(min = 2, max = 150, message = "Faculty name must be between 2 and 150 characters")
    private String facultyName;

    @Size(max = 150, message = "Department name must not exceed 150 characters")
    private String department;

    public CreateFacultyRequest() {
    }

    public CreateFacultyRequest(String facultyName, String department) {
        this.facultyName = facultyName;
        this.department = department;
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
