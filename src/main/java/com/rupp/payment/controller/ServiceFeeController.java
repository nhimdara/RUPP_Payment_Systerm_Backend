package com.rupp.payment.controller;

import com.rupp.payment.dto.request.PayServiceFeeRequest;
import com.rupp.payment.dto.request.ServiceFeeRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ServiceFeeResponse;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.ServiceFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-fees")
@Tag(name = "Campus Service Fees", description = "Moto parking, library, lab and other non-tuition fees")
public class ServiceFeeController {

    private final ServiceFeeService serviceFeeService;
    private final AuthService authService;

    public ServiceFeeController(ServiceFeeService serviceFeeService, AuthService authService) {
        this.serviceFeeService = serviceFeeService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "List active service fees")
    public ResponseEntity<ApiResponse<List<ServiceFeeResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success("Service fees fetched successfully", serviceFeeService.listActive()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "List service fees with my payment status",
            description = "myStatus is NOT_PAID, PENDING, VERIFIED or PAID for the logged-in student.")
    public ResponseEntity<ApiResponse<List<ServiceFeeResponse>>> listMine() {
        return ResponseEntity.ok(ApiResponse.success("My service fees fetched successfully",
                serviceFeeService.listForStudent(authService.getCurrentAuthenticatedUser())));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Pay a service fee",
            description = "Creates a PENDING payment for the fee amount. Verify it with the OTP endpoints, then Finance confirms it.")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(@PathVariable Long id,
                                                            @Valid @RequestBody PayServiceFeeRequest request) {
        PaymentResponse response = serviceFeeService.pay(authService.getCurrentAuthenticatedUser(), id, request);
        return new ResponseEntity<>(ApiResponse.success("Service payment submitted successfully", response), HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a service fee (Admin)")
    public ResponseEntity<ApiResponse<ServiceFeeResponse>> create(@Valid @RequestBody ServiceFeeRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Service fee created", serviceFeeService.create(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update a service fee (Admin)")
    public ResponseEntity<ApiResponse<ServiceFeeResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody ServiceFeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service fee updated", serviceFeeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Deactivate a service fee (Admin)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        serviceFeeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Service fee deactivated"));
    }
}
