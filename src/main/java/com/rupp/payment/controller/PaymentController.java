package com.rupp.payment.controller;

<<<<<<< HEAD
import com.rupp.payment.dto.request.MakePaymentRequest;
import com.rupp.payment.dto.request.VerifyPaymentRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.PaymentService;
=======
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
>>>>>>> origin/feature/search
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
<<<<<<< HEAD
import org.springframework.format.annotation.DateTimeFormat;
=======
import org.springframework.data.domain.Pageable;
>>>>>>> origin/feature/search
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payment Management", description = "Endpoints for payment processing, verification, and payment history")
=======
@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payment Management", description = "Endpoints for payment creation, multi-param search, status transitions, and receipts")
>>>>>>> origin/feature/search
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthService authService;

    public PaymentController(PaymentService paymentService, AuthService authService) {
        this.paymentService = paymentService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
<<<<<<< HEAD
    @Operation(summary = "Make payment", description = "Student submits a tuition fee payment with gateway details (ABA PayWay, KHQR, etc.).")
    public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(@Valid @RequestBody MakePaymentRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.makePayment(currentUser, request);
        return new ResponseEntity<>(ApiResponse.success("Payment submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "View my payment status", description = "Student views the status and details of their submitted payments.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<PaymentResponse> list = paymentService.getMyPayments(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payments fetched successfully", list));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "View payment history", description = "Student views chronological payment history.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPaymentHistory() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<PaymentResponse> list = paymentService.getMyPayments(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment history fetched successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve a payment record by its ID. Students can view their own; Finance Staff and Admin can view any.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.getPaymentById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", response));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "View payment status", description = "View the current verification and confirmation status of a payment.")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatusById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.getPaymentById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment status fetched successfully", response.getPaymentStatus()));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Manage payment records", description = "Finance Staff & Admin can search, filter, and view all student payment records.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<PaymentResponse> list = paymentService.getAllPayments(studentId, statusId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Payment records fetched successfully", list));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Verify payment", description = "Finance Staff verifies payment reference and details, marking status as VERIFIED.")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @PathVariable Long id,
            @RequestBody(required = false) VerifyPaymentRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.verifyPayment(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Confirm payment", description = "Finance Staff confirms payment after verification, updating status to CONFIRMED and automatically issuing the Receipt.")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.confirmPayment(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed and receipt issued successfully", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Reject payment", description = "Finance Staff rejects invalid payment submission.")
    public ResponseEntity<ApiResponse<PaymentResponse>> rejectPayment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Payment verification failed") String reason) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        PaymentResponse response = paymentService.rejectPayment(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment rejected successfully", response));
    }

    @GetMapping("/statuses")
    @Operation(summary = "List all payment statuses", description = "Retrieve list of all possible payment statuses (PENDING, VERIFIED, CONFIRMED, REJECTED).")
    public ResponseEntity<ApiResponse<List<PaymentStatusResponse>>> getAllPaymentStatuses() {
        List<PaymentStatusResponse> list = paymentService.getAllPaymentStatuses();
        return ResponseEntity.ok(ApiResponse.success("Payment statuses fetched successfully", list));
=======
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
>>>>>>> origin/feature/search
    }
}
