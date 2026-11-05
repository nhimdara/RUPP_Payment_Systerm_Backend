package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.NavigationTabResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.DashboardService;
import com.rupp.payment.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/navigation")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Navigation", description = "Endpoints configuring bottom navigation bar tabs, dynamic badge counts, and main screen route maps")
public class NavigationController {

    private final AuthService authService;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;

    public NavigationController(AuthService authService,
                                NotificationService notificationService,
                                DashboardService dashboardService) {
        this.authService = authService;
        this.notificationService = notificationService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/tabs")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get bottom navigation bar tabs with live badges", description = "Returns the 4 core bottom navigation tabs with live dynamic unread counters and status indicators for the student.")
    public ResponseEntity<ApiResponse<List<NavigationTabResponse>>> getBottomNavTabs() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long unreadCount = notificationService.getUnreadCount(currentUser);

        List<NavigationTabResponse> tabs = Arrays.asList(
                new NavigationTabResponse(
                        "HOME",
                        "Home",
                        "/screens/home",
                        unreadCount > 0 ? (int) unreadCount : null,
                        "ACTIVE",
                        true
                ),
                new NavigationTabResponse(
                        "TUITION",
                        "Tuition",
                        "/screens/tuition",
                        null,
                        "ACTIVE",
                        false
                ),
                new NavigationTabResponse(
                        "HISTORY",
                        "History",
                        "/screens/history",
                        null,
                        "ACTIVE",
                        false
                ),
                new NavigationTabResponse(
                        "PROFILE",
                        "Profile",
                        "/screens/profile",
                        null,
                        "ACTIVE",
                        false
                )
        );

        return ResponseEntity.ok(ApiResponse.success("Navigation tabs loaded successfully", tabs));
    }

    @GetMapping("/routes")
    @Operation(summary = "Get screen navigation catalog", description = "Provides navigation metadata mapping quick services, bottom bar, and detail screens.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNavigationRoutes() {
        Map<String, Object> routes = Map.of(
                "mainScreens", List.of(
                        Map.of("id", "home", "title", "Student Home Dashboard", "route", "/screens/home"),
                        Map.of("id", "tuition", "title", "Tuition Fee & Balance", "route", "/screens/tuition"),
                        Map.of("id", "history", "title", "Payment History & Records", "route", "/screens/history"),
                        Map.of("id", "profile", "title", "Student Profile & Settings", "route", "/screens/profile")
                ),
                "supportingScreens", List.of(
                        Map.of("id", "notifications", "title", "Notifications & Alerts", "route", "/screens/notifications"),
                        Map.of("id", "receipt_viewer", "title", "Official PDF Receipt Viewer", "route", "/screens/receipts/{id}"),
                        Map.of("id", "payment_gateway", "title", "KHQR & ABA Payment Gateway", "route", "/screens/pay")
                )
        );
        return ResponseEntity.ok(ApiResponse.success("Navigation routes catalog loaded", routes));
    }
}
