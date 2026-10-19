package com.rupp.payment.controller;

import com.rupp.payment.dto.request.CreateSemesterRequest;
import com.rupp.payment.dto.request.UpdateSemesterRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.SemesterResponse;
import com.rupp.payment.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Semester Management", description = "CRUD operations for academic terms and semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    @Operation(summary = "List all semesters", description = "Retrieve all semesters, optionally filtered by academic year ID.")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getAllSemesters(
            @RequestParam(required = false) Long academicYearId) {
        List<SemesterResponse> semesters = semesterService.getAllSemesters(academicYearId);
        return ResponseEntity.ok(ApiResponse.success("Semesters fetched successfully", semesters));
    }

    @GetMapping("/active")
    @Operation(summary = "Get current active semester", description = "Retrieve the currently active university semester.")
    public ResponseEntity<ApiResponse<SemesterResponse>> getActiveSemester() {
        SemesterResponse response = semesterService.getActiveSemester();
        return ResponseEntity.ok(ApiResponse.success("Active semester fetched successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get semester by ID", description = "Retrieve a specific semester by its ID.")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemesterById(@PathVariable Long id) {
        SemesterResponse response = semesterService.getSemesterById(id);
        return ResponseEntity.ok(ApiResponse.success("Semester fetched successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Create semester", description = "Create a new semester linked to an academic year. Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(
            @Valid @RequestBody CreateSemesterRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return new ResponseEntity<>(ApiResponse.success("Semester created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Update semester", description = "Update semester details, dates, or active status. Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSemesterRequest request) {
        SemesterResponse response = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(ApiResponse.success("Semester updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete semester", description = "Delete a semester by its ID. Requires Admin authority.")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.ok(ApiResponse.success("Semester deleted successfully", null));
    }
}
