package com.rupp.payment.controller;

import com.rupp.payment.dto.request.CreatePaymentRequest;
import com.rupp.payment.dto.request.PaymentSearchCriteria;
import com.rupp.payment.dto.request.UpdatePaymentStatusRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ReceiptResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.PaymentService;
import com.rupp.payment.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payment Management", description = "Endpoints for payment creation, multi-param search, status transitions, and receipts")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthService authService;
    private final com.rupp.payment.service.ReceiptService receiptService;

    public PaymentController(PaymentService paymentService, AuthService authService,
                             com.rupp.payment.service.ReceiptService receiptService) {
        this.paymentService = paymentService;
        this.authService = authService;
        this.receiptService = receiptService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Make payment", description = "Student submits a tuition fee payment with transaction slip/reference. Initial status is PENDING.")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.createPayment(currentUser, request);
        return new ResponseEntity<>(ApiResponse.success("Payment submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Search payments (multi-param specifications)", description = "Search and filter payments dynamically by student, faculty, academic year, status, date range, amount range, and payment method.")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> searchPayments(
            @ModelAttribute PaymentSearchCriteria criteria,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        PagedResponse<PaymentResponse> response = paymentService.searchPayments(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "View payment history", description = "Allows logged-in students to view their own paginated payment history.")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User currentUser = authService.getCurrentAuthenticatedUser();
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        PagedResponse<PaymentResponse> response = paymentService.getStudentPayments(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Student payments retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF', 'ROLE_STUDENT')")
    @Operation(summary = "Get payment by ID", description = "Retrieve full payment details including status, student info, and payment details.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Update payment status (State Machine)", description = "Finance Staff and Admin transition payment status (e.g., PENDING -> VERIFIED -> CONFIRMED, or REJECTED). Confirmed status auto-generates receipt.")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentResponse response = paymentService.updatePaymentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", response));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF', 'ROLE_STUDENT')")
    @Operation(summary = "Get payment receipt", description = "Retrieve the official receipt for a confirmed payment.")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getPaymentReceipt(@PathVariable Long id) {
        ReceiptResponse response = paymentService.getReceiptByPaymentId(id);
        return ResponseEntity.ok(ApiResponse.success("Receipt retrieved successfully", response));
    }

    @GetMapping(value = "/{id}/receipt/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF', 'ROLE_STUDENT')")
    @Operation(summary = "Export payment receipt as PDF", description = "Generates and exports the official receipt PDF for a payment.")
    public ResponseEntity<byte[]> getPaymentReceiptPdf(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        byte[] pdfBytes = receiptService.generateReceiptPdfByPaymentId(id, currentUser);
        String filename = "receipt-payment-" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
