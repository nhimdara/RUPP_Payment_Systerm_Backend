package com.rupp.payment.controller;

import com.rupp.payment.dto.request.DevPaymentCreateRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.DevPaymentResponse;
import com.rupp.payment.security.Idempotent;
import com.rupp.payment.security.IdempotencyInterceptor;
import com.rupp.payment.service.DevPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Off unless payment.security.dev-endpoints.enabled=true; remove once the real Payment API exists
@RestController
@RequestMapping("/api/v1/dev/payments")
@ConditionalOnProperty(name = "payment.security.dev-endpoints.enabled", havingValue = "true")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dev: Make Payment", description = "Temporary stand-in for the Payment API so the OTP and confirmation flow can be tested")
public class DevPaymentController {

    private final DevPaymentService devPaymentService;

    public DevPaymentController(DevPaymentService devPaymentService) {
        this.devPaymentService = devPaymentService;
    }

    @Idempotent
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Create a PENDING payment", description = "Blocked with 409 if the student already has an active payment for the fee structure.")
    @Parameter(in = ParameterIn.HEADER, name = IdempotencyInterceptor.IDEMPOTENCY_KEY_HEADER, required = true,
            schema = @Schema(type = "string", format = "uuid"))
    public ResponseEntity<ApiResponse<DevPaymentResponse>> create(@Valid @RequestBody DevPaymentCreateRequest request) {
        DevPaymentResponse response = devPaymentService.create(request);
        return new ResponseEntity<>(ApiResponse.success("Payment created", response), HttpStatus.CREATED);
    }
}
