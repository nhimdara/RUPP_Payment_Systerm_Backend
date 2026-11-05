package com.rupp.payment.service;

import com.rupp.payment.dto.response.NotificationResponse;
import com.rupp.payment.entity.Notification;
import com.rupp.payment.entity.NotificationCategory;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User studentUser;
    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(5L);
        studentUser.setUsername("leangkhai");
        studentUser.setEmail("leangkhai@rupp.edu.kh");
        studentUser.setRole(Role.ROLE_STUDENT);

        notification1 = new Notification(
                studentUser,
                "Tuition Payment Confirmed",
                "Your tuition payment was verified.",
                NotificationCategory.PAYMENTS,
                new BigDecimal("600.00"),
                "RUPP-REC-2026-002"
        );
        notification1.setId(101L);

        notification2 = new Notification(
                studentUser,
                "Semester 1 Schedule",
                "Timetable released.",
                NotificationCategory.ACADEMIC
        );
        notification2.setId(102L);
    }

    @Test
    @DisplayName("Get all notifications for user")
    void getMyNotifications_all_returnsList() {
        when(notificationRepository.findByUserOrderByCreatedAtDesc(studentUser))
                .thenReturn(List.of(notification1, notification2));

        List<NotificationResponse> result = notificationService.getMyNotifications(studentUser, null);

        assertEquals(2, result.size());
        assertEquals("Tuition Payment Confirmed", result.get(0).getTitle());
        verify(notificationRepository, times(1)).findByUserOrderByCreatedAtDesc(studentUser);
    }

    @Test
    @DisplayName("Get notifications filtered by category")
    void getMyNotifications_filteredByCategory_returnsFiltered() {
        when(notificationRepository.findByUserAndCategoryOrderByCreatedAtDesc(studentUser, NotificationCategory.PAYMENTS))
                .thenReturn(List.of(notification1));

        List<NotificationResponse> result = notificationService.getMyNotifications(studentUser, NotificationCategory.PAYMENTS);

        assertEquals(1, result.size());
        assertEquals(NotificationCategory.PAYMENTS, result.get(0).getCategory());
        verify(notificationRepository, times(1)).findByUserAndCategoryOrderByCreatedAtDesc(studentUser, NotificationCategory.PAYMENTS);
    }

    @Test
    @DisplayName("Get unread count returns positive number")
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserAndIsReadFalse(studentUser)).thenReturn(3L);

        long count = notificationService.getUnreadCount(studentUser);

        assertEquals(3L, count);
        verify(notificationRepository).countByUserAndIsReadFalse(studentUser);
    }

    @Test
    @DisplayName("Mark notification as read updates isRead flag")
    void markAsRead_success() {
        when(notificationRepository.findById(101L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(studentUser, 101L);

        assertTrue(response.isRead());
        verify(notificationRepository).save(notification1);
    }

    @Test
    @DisplayName("Mark notification as read throws exception when not found")
    void markAsRead_notFound_throwsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(studentUser, 999L));
    }

    @Test
    @DisplayName("Mark all as read calls repository bulk update")
    void markAllAsRead_callsRepository() {
        notificationService.markAllAsRead(studentUser);

        verify(notificationRepository).markAllAsReadForUser(studentUser);
    }
}
