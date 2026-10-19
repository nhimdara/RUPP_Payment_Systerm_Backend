package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.FacultyRevenueReport;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.PaymentMethodReport;
import com.rupp.payment.service.FinancialReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Financial Reports", description = "Endpoints for Finance Staff and Admin to generate financial reports")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    public FinancialReportController(FinancialReportService financialReportService) {
        this.financialReportService = financialReportService;
    }

    @GetMapping("/financial-summary")
    @Operation(summary = "Generate financial summary report", description = "Finance Staff generates high-level financial metrics, total revenue collected, and payment counts.")
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary() {
        FinancialSummaryResponse summary = financialReportService.getFinancialSummary();
        return ResponseEntity.ok(ApiResponse.success("Financial summary report generated successfully", summary));
    }

    @GetMapping("/faculty-breakdown")
    @Operation(summary = "Generate faculty revenue report", description = "Finance Staff generates revenue breakdown by academic faculty.")
    public ResponseEntity<ApiResponse<List<FacultyRevenueReport>>> getFacultyRevenueReports() {
        List<FacultyRevenueReport> report = financialReportService.getFacultyRevenueReports();
        return ResponseEntity.ok(ApiResponse.success("Faculty revenue report generated successfully", report));
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Generate payment methods report", description = "Finance Staff generates payment volume breakdown by payment method.")
    public ResponseEntity<ApiResponse<List<PaymentMethodReport>>> getPaymentMethodReports() {
        List<PaymentMethodReport> report = financialReportService.getPaymentMethodReports();
        return ResponseEntity.ok(ApiResponse.success("Payment method report generated successfully", report));
    }

    @GetMapping("/daily-revenue")
    @Operation(summary = "Generate daily revenue report", description = "Finance Staff & Admin view daily aggregated revenue metrics and transaction volumes.")
    public ResponseEntity<ApiResponse<List<com.rupp.payment.dto.response.DailyRevenueReport>>> getDailyRevenue(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        List<com.rupp.payment.dto.response.DailyRevenueReport> report = financialReportService.getDailyRevenueReports(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Daily revenue report generated successfully", report));
    }

    @GetMapping("/monthly-revenue")
    @Operation(summary = "Generate monthly revenue report", description = "Finance Staff & Admin view monthly aggregated revenue metrics for a given year.")
    public ResponseEntity<ApiResponse<List<com.rupp.payment.dto.response.MonthlyRevenueReport>>> getMonthlyRevenue(
            @RequestParam(required = false) Integer year) {
        List<com.rupp.payment.dto.response.MonthlyRevenueReport> report = financialReportService.getMonthlyRevenueReports(year);
        return ResponseEntity.ok(ApiResponse.success("Monthly revenue report generated successfully", report));
    }
}
