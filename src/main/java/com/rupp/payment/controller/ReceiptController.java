package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.ReceiptResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/receipts")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Receipt Management", description = "Endpoints for viewing and managing official payment receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final AuthService authService;

    public ReceiptController(ReceiptService receiptService, AuthService authService) {
        this.receiptService = receiptService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Manage receipts", description = "Finance Staff & Admin can list, filter, and manage all issued payment receipts.")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAllReceipts(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<ReceiptResponse> list = receiptService.getAllReceipts(studentId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Receipts fetched successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receipt by ID", description = "Retrieve a receipt by its unique ID. Students can view their own; Finance Staff and Admin can view any.")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ReceiptResponse response = receiptService.getReceiptById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Receipt fetched successfully", response));
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "View receipt for payment", description = "Retrieve the receipt generated for a specific payment ID.")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptByPaymentId(@PathVariable Long paymentId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ReceiptResponse response = receiptService.getReceiptByPaymentId(paymentId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Receipt fetched successfully", response));
    }

    @GetMapping("/number/{receiptNumber}")
    @Operation(summary = "Get receipt by receipt number", description = "Retrieve a receipt using its official receipt number.")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptByNumber(@PathVariable String receiptNumber) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ReceiptResponse response = receiptService.getReceiptByNumber(receiptNumber, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Receipt fetched successfully", response));
    }
}
