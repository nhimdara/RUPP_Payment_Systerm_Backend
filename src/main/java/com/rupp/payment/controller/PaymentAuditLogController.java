package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.AuditLogPageResponse;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.service.AuditLogFilter;
import com.rupp.payment.service.PaymentAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
@Tag(name = "Payment Audit Log", description = "Append-only trail of payment, OTP and idempotency events (Monitor System / Manage Payment Records)")
public class PaymentAuditLogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PaymentAuditService auditService;

    public PaymentAuditLogController(PaymentAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/payments/{paymentId}/audit-logs")
    @Operation(summary = "Payment timeline", description = "All audit events for one payment, oldest first.")
    public ResponseEntity<ApiResponse<AuditLogPageResponse>> paymentTimeline(@PathVariable Long paymentId,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "50") int size) {
        AuditLogPageResponse response = auditService.search(AuditLogFilter.forPayment(paymentId),
                pageRequest(page, size, Sort.Direction.ASC));
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Search audit logs", description = "Filter by action, actor, payment and time range, newest first.")
    public ResponseEntity<ApiResponse<AuditLogPageResponse>> search(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuditLogFilter filter = new AuditLogFilter(paymentId, action, actorUserId, from, to);
        AuditLogPageResponse response = auditService.search(filter, pageRequest(page, size, Sort.Direction.DESC));
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }

    private static Pageable pageRequest(int page, int size, Sort.Direction direction) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize, Sort.by(direction, "createdAt", "id"));
    }
}
