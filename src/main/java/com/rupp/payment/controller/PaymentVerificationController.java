package com.rupp.payment.controller;

import com.rupp.payment.dto.request.OtpVerifyRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.OtpChallengeResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.service.PaymentVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
@Tag(name = "Payment Verification (OTP)", description = "Student verifies their own pending payment with a one-time code")
public class PaymentVerificationController {

    private final PaymentVerificationService verificationService;

    public PaymentVerificationController(PaymentVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/{paymentId}/verification/otp")
    @Operation(summary = "Request an OTP", description = "Sends a 6-digit code (valid 5 minutes) for a PENDING payment owned by the student.")
    public ResponseEntity<ApiResponse<OtpChallengeResponse>> requestOtp(@PathVariable Long paymentId) {
        OtpChallengeResponse response = verificationService.requestOtp(paymentId);
        return new ResponseEntity<>(ApiResponse.success("OTP sent", response), HttpStatus.CREATED);
    }

    @PostMapping("/verification/otp/{challengeId}/resend")
    @Operation(summary = "Resend the OTP", description = "Replaces the code; allowed once every 60 seconds. Failed attempts are not reset.")
    public ResponseEntity<ApiResponse<OtpChallengeResponse>> resendOtp(@PathVariable UUID challengeId) {
        OtpChallengeResponse response = verificationService.resendOtp(challengeId);
        return ResponseEntity.ok(ApiResponse.success("OTP resent", response));
    }

    @PostMapping("/verification/otp/{challengeId}/verify")
    @Operation(summary = "Verify the OTP", description = "A correct code moves the payment from PENDING to VERIFIED. 5 wrong codes lock verification for 15 minutes.")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> verifyOtp(@PathVariable UUID challengeId,
                                                                        @Valid @RequestBody OtpVerifyRequest request) {
        PaymentStatusResponse response = verificationService.verifyOtp(challengeId, request.code());
        return ResponseEntity.ok(ApiResponse.success("Payment verified", response));
    }
}
