package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.NotificationResponse;
import com.rupp.payment.entity.NotificationCategory;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Notifications", description = "Student notifications screen endpoints for real-time payment alerts, academic updates, and university bulletins")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get student notifications", description = "Retrieve list of notifications for the logged-in student, optionally filtered by category (PAYMENTS, ACADEMIC, ANNOUNCEMENTS, SYSTEM).")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(required = false) NotificationCategory category) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<NotificationResponse> list = notificationService.getMyNotifications(currentUser, category);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully", list));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Get unread notifications count", description = "Returns the count of unread notifications for badge indicators.")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by notification ID.")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        NotificationResponse response = notificationService.markAsRead(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for the logged-in student as read.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
