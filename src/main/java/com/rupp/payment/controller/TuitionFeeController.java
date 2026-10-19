package com.rupp.payment.controller;

import com.rupp.payment.dto.request.CreateTuitionFeeRequest;
import com.rupp.payment.dto.request.UpdateTuitionFeeRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.TuitionFeeResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.TuitionFeeService;
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
@RequestMapping("/api/v1/tuition-fees")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Tuition Fee Management", description = "Endpoints for managing tuition fees and viewing fee structures")
public class TuitionFeeController {

    private final TuitionFeeService tuitionFeeService;
    private final AuthService authService;

    public TuitionFeeController(TuitionFeeService tuitionFeeService, AuthService authService) {
        this.tuitionFeeService = tuitionFeeService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "List all tuition fee structures", description = "Retrieve tuition fee structures with optional filters by faculty and academic year.")
    public ResponseEntity<ApiResponse<List<TuitionFeeResponse>>> getAllTuitionFees(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long academicYearId) {
        List<TuitionFeeResponse> list = tuitionFeeService.getAllTuitionFees(facultyId, academicYearId);
        return ResponseEntity.ok(ApiResponse.success("Tuition fees fetched successfully", list));
    }

    @GetMapping("/my-fee")
    @Operation(summary = "View current student's tuition fee", description = "Allows a logged-in student to view their applicable tuition fee based on their enrolled faculty and academic year.")
    public ResponseEntity<ApiResponse<TuitionFeeResponse>> getMyTuitionFee() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        TuitionFeeResponse fee = tuitionFeeService.getMyTuitionFee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("My tuition fee fetched successfully", fee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tuition fee structure by ID", description = "Retrieve a specific tuition fee structure.")
    public ResponseEntity<ApiResponse<TuitionFeeResponse>> getTuitionFeeById(@PathVariable Long id) {
        TuitionFeeResponse fee = tuitionFeeService.getTuitionFeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Tuition fee fetched successfully", fee));
    }

    @GetMapping("/calculate")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Calculate current student tuition balance", description = "Computes tuition fee breakdown, payments made, remaining balance, and payment status for the logged-in student.")
    public ResponseEntity<ApiResponse<com.rupp.payment.dto.response.TuitionCalculationResponse>> calculateMyTuition() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        com.rupp.payment.dto.response.TuitionCalculationResponse response = tuitionFeeService.calculateMyTuition(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Tuition calculation completed successfully", response));
    }

    @GetMapping("/calculate/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Calculate student tuition balance by ID", description = "Finance Staff & Admin compute tuition fee balance, paid history, and status for any student.")
    public ResponseEntity<ApiResponse<com.rupp.payment.dto.response.TuitionCalculationResponse>> calculateStudentTuition(@PathVariable Long studentId) {
        com.rupp.payment.dto.response.TuitionCalculationResponse response = tuitionFeeService.calculateStudentTuition(studentId);
        return ResponseEntity.ok(ApiResponse.success("Tuition calculation completed successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create tuition fee structure", description = "Admin sets up a new tuition fee structure for a faculty and academic year.")
    public ResponseEntity<ApiResponse<TuitionFeeResponse>> createTuitionFee(@Valid @RequestBody CreateTuitionFeeRequest request) {
        TuitionFeeResponse created = tuitionFeeService.createTuitionFee(request);
        return new ResponseEntity<>(ApiResponse.success("Tuition fee created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update tuition fee structure", description = "Admin updates an existing tuition fee structure.")
    public ResponseEntity<ApiResponse<TuitionFeeResponse>> updateTuitionFee(@PathVariable Long id,
                                                                           @Valid @RequestBody UpdateTuitionFeeRequest request) {
        TuitionFeeResponse updated = tuitionFeeService.updateTuitionFee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tuition fee updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete tuition fee structure", description = "Admin deletes a tuition fee structure.")
    public ResponseEntity<ApiResponse<Void>> deleteTuitionFee(@PathVariable Long id) {
        tuitionFeeService.deleteTuitionFee(id);
        return ResponseEntity.ok(ApiResponse.success("Tuition fee deleted successfully", null));
    }
}
