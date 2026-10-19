package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Major;

public class MajorResponse {

    private Long id;
    private String majorName;
    private String majorCode;
    private Faculty faculty;
    private String description;

    public MajorResponse() {
    }

    public MajorResponse(Long id, String majorName, String majorCode, Faculty faculty, String description) {
        this.id = id;
        this.majorName = majorName;
        this.majorCode = majorCode;
        this.faculty = faculty;
        this.description = description;
    }

    public static MajorResponse fromEntity(Major entity) {
        if (entity == null) return null;
        return new MajorResponse(
                entity.getId(),
                entity.getMajorName(),
                entity.getMajorCode(),
                entity.getFaculty(),
                entity.getDescription()
        );
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
