package com.rupp.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateMajorRequest {

    @NotBlank(message = "Major name is required")
    private String majorName;

    @NotBlank(message = "Major code is required (e.g. 'CS', 'ITE')")
    @Size(max = 50, message = "Major code cannot exceed 50 characters")
    private String majorCode;

    private Long facultyId;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public CreateMajorRequest() {
    }

    public CreateMajorRequest(String majorName, String majorCode, Long facultyId, String description) {
        this.majorName = majorName;
        this.majorCode = majorCode;
        this.facultyId = facultyId;
        this.description = description;
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

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
