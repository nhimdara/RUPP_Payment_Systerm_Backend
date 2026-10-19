package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.SystemMonitoringResponse;
import com.rupp.payment.service.SystemMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/monitoring")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "System Monitoring", description = "Admin endpoints for system health and resource monitoring")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SystemMonitoringController {

    private final SystemMonitoringService systemMonitoringService;

    public SystemMonitoringController(SystemMonitoringService systemMonitoringService) {
        this.systemMonitoringService = systemMonitoringService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Monitor system", description = "Admin monitors system uptime, JVM memory, database connectivity, and total entity counts.")
    public ResponseEntity<ApiResponse<SystemMonitoringResponse>> getSystemMetrics() {
        SystemMonitoringResponse stats = systemMonitoringService.getSystemMetrics();
        return ResponseEntity.ok(ApiResponse.success("System monitoring metrics retrieved successfully", stats));
    }
}
