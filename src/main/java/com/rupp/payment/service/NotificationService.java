package com.rupp.payment.service;

import com.rupp.payment.dto.response.NotificationResponse;
import com.rupp.payment.entity.Notification;
import com.rupp.payment.entity.NotificationCategory;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User currentUser, NotificationCategory category) {
        List<Notification> notifications;
        if (category != null) {
            notifications = notificationRepository.findByUserAndCategoryOrderByCreatedAtDesc(currentUser, category);
        } else {
            notifications = notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);
        }
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    @Transactional
    public NotificationResponse markAsRead(User currentUser, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notification not found for current user with id: " + notificationId);
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Transactional
    public void markAllAsRead(User currentUser) {
        notificationRepository.markAllAsReadForUser(currentUser);
    }

    @Transactional
    public NotificationResponse createNotification(User user, String title, String message,
                                                  NotificationCategory category, BigDecimal amount, String receiptNumber) {
        Notification notification = new Notification(user, title, message, category, amount, receiptNumber);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }
}
