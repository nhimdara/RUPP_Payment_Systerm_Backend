package com.rupp.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class SelectAcademicInfoRequest {

    @NotNull(message = "Faculty ID is required")
    @Schema(description = "ID of the selected faculty", example = "1")
    private Long facultyId;

    @NotNull(message = "Academic year ID is required")
    @Schema(description = "ID of the selected academic year", example = "1")
    private Long academicYearId;

    @Schema(description = "Optional ID of the selected major", example = "1")
    private Long majorId;

    public SelectAcademicInfoRequest() {
    }

    public SelectAcademicInfoRequest(Long facultyId, Long academicYearId, Long majorId) {
        this.facultyId = facultyId;
        this.academicYearId = academicYearId;
        this.majorId = majorId;
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
