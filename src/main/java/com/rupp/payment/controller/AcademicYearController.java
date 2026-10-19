package com.rupp.payment.controller;

import com.rupp.payment.dto.request.CreateAcademicYearRequest;
import com.rupp.payment.dto.request.UpdateAcademicYearRequest;
import com.rupp.payment.dto.response.AcademicYearResponse;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.service.AcademicYearService;
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
@RequestMapping("/api/v1/academic-years")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Academic Year Management", description = "CRUD operations for university academic years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping
    @Operation(summary = "List all academic years", description = "Retrieve all registered academic years.")
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAllAcademicYears() {
        List<AcademicYearResponse> academicYears = academicYearService.getAllAcademicYears();
        return ResponseEntity.ok(ApiResponse.success("Academic years fetched successfully", academicYears));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academic year by ID", description = "Retrieve a specific academic year by its ID.")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getAcademicYearById(@PathVariable Long id) {
        AcademicYearResponse response = academicYearService.getAcademicYearById(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year fetched successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Create academic year", description = "Create a new academic year (e.g., 2024-2025). Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> createAcademicYear(
            @Valid @RequestBody CreateAcademicYearRequest request) {
        AcademicYearResponse response = academicYearService.createAcademicYear(request);
        return new ResponseEntity<>(ApiResponse.success("Academic year created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Update academic year", description = "Update an existing academic year. Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> updateAcademicYear(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAcademicYearRequest request) {
        AcademicYearResponse response = academicYearService.updateAcademicYear(id, request);
        return ResponseEntity.ok(ApiResponse.success("Academic year updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete academic year", description = "Delete an academic year by ID. Requires Admin authority.")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year deleted successfully", null));
    }
}
