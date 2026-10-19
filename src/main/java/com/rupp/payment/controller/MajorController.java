package com.rupp.payment.controller;

import com.rupp.payment.dto.request.CreateMajorRequest;
import com.rupp.payment.dto.request.UpdateMajorRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.MajorResponse;
import com.rupp.payment.service.MajorService;
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
@RequestMapping("/api/v1/majors")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Major Management", description = "CRUD operations for academic majors / study programs")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    @Operation(summary = "List all majors", description = "Retrieve all majors, optionally filtered by faculty ID.")
    public ResponseEntity<ApiResponse<List<MajorResponse>>> getAllMajors(
            @RequestParam(required = false) Long facultyId) {
        List<MajorResponse> majors = majorService.getAllMajors(facultyId);
        return ResponseEntity.ok(ApiResponse.success("Majors fetched successfully", majors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get major by ID", description = "Retrieve a specific major by its ID.")
    public ResponseEntity<ApiResponse<MajorResponse>> getMajorById(@PathVariable Long id) {
        MajorResponse response = majorService.getMajorById(id);
        return ResponseEntity.ok(ApiResponse.success("Major fetched successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Create major", description = "Create a new major/program linked optionally to a faculty. Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<MajorResponse>> createMajor(@Valid @RequestBody CreateMajorRequest request) {
        MajorResponse response = majorService.createMajor(request);
        return new ResponseEntity<>(ApiResponse.success("Major created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Update major", description = "Update major details such as name, code, faculty, or description. Requires Admin or Finance Staff.")
    public ResponseEntity<ApiResponse<MajorResponse>> updateMajor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMajorRequest request) {
        MajorResponse response = majorService.updateMajor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Major updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete major", description = "Delete a major by its ID. Requires Admin authority.")
    public ResponseEntity<ApiResponse<Void>> deleteMajor(@PathVariable Long id) {
        majorService.deleteMajor(id);
        return ResponseEntity.ok(ApiResponse.success("Major deleted successfully", null));
    }
}
