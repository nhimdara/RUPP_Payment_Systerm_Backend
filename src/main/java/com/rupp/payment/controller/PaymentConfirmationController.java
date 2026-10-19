package com.rupp.payment.controller;

import com.rupp.payment.dto.request.PaymentConfirmRequest;
import com.rupp.payment.dto.request.PaymentRejectRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.security.Idempotent;
import com.rupp.payment.security.IdempotencyInterceptor;
import com.rupp.payment.service.PaymentConfirmationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAuthority('ROLE_FINANCE_STAFF')")
@Tag(name = "Payment Confirmation", description = "Finance staff confirm or reject payments that students verified by OTP")
public class PaymentConfirmationController {

    private final PaymentConfirmationService confirmationService;

    public PaymentConfirmationController(PaymentConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @Idempotent
    @PostMapping("/{paymentId}/confirm")
    @Operation(summary = "Confirm a verified payment", description = "Moves a VERIFIED payment to CONFIRMED. Requires an Idempotency-Key header.")
    @Parameter(in = ParameterIn.HEADER, name = IdempotencyInterceptor.IDEMPOTENCY_KEY_HEADER, required = true,
            schema = @Schema(type = "string", format = "uuid"))
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> confirm(@PathVariable Long paymentId,
                                                                      @Valid @RequestBody(required = false) PaymentConfirmRequest request) {
        String note = request == null ? null : request.note();
        PaymentStatusResponse response = confirmationService.confirm(paymentId, note);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed", response));
    }

    @Idempotent
    @PostMapping("/{paymentId}/reject")
    @Operation(summary = "Reject a payment", description = "Moves a PENDING or VERIFIED payment to REJECTED. Requires an Idempotency-Key header.")
    @Parameter(in = ParameterIn.HEADER, name = IdempotencyInterceptor.IDEMPOTENCY_KEY_HEADER, required = true,
            schema = @Schema(type = "string", format = "uuid"))
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> reject(@PathVariable Long paymentId,
                                                                     @Valid @RequestBody PaymentRejectRequest request) {
        PaymentStatusResponse response = confirmationService.reject(paymentId, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Payment rejected", response));
    }
}
