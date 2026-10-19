package com.rupp.payment.controller;

import com.rupp.payment.dto.request.ReportSearchCriteria;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.service.ReportService;
import com.rupp.payment.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Report Management", description = "Endpoints for generating financial reports and summaries with dynamic filtering")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Get financial summary report", description = "Aggregate financial metrics: total revenue, pending amounts, transaction counts by status, faculty revenue breakdown, and payment method statistics.")
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary(
            @ModelAttribute ReportSearchCriteria criteria) {
        FinancialSummaryResponse summary = reportService.getFinancialSummary(criteria);
        return ResponseEntity.ok(ApiResponse.success("Financial summary report generated successfully", summary));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Get detailed payments report", description = "Paginated list of payments matching report search filters (date range, faculty, academic year, status).")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPaymentsReport(
            @ModelAttribute ReportSearchCriteria criteria,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        PagedResponse<PaymentResponse> report = reportService.getFilteredPaymentReport(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Payments report generated successfully", report));
    }
}
