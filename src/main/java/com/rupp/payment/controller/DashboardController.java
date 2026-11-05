package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.StudentDashboardResponse;
import com.rupp.payment.dto.response.StudentProfileSummaryCardResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Home & Dashboard", description = "Endpoints serving data for the Student Home Dashboard and Student Profile Summary Card")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/student")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get student home dashboard", description = "Returns aggregated data for the Student Home Dashboard, including greeting, summary card, quick action hub, recent payments, and campus bulletins.")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        StudentDashboardResponse response = dashboardService.getStudentDashboard(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Student dashboard loaded successfully", response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Get student home dashboard by student ID", description = "Finance Staff & Admin can view the dashboard view for any student.")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboardById(@PathVariable Long studentId) {
        StudentDashboardResponse response = dashboardService.getStudentDashboardById(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student dashboard loaded successfully", response));
    }

    @GetMapping("/student/summary-card")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get student profile summary card", description = "Returns standalone card data containing student academic credentials and real-time tuition balance snapshot.")
    public ResponseEntity<ApiResponse<StudentProfileSummaryCardResponse>> getProfileSummaryCard() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        StudentProfileSummaryCardResponse response = dashboardService.getProfileSummaryCard(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile summary card loaded successfully", response));
    }
}
