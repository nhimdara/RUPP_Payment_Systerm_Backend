package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Notification;
import com.rupp.payment.entity.NotificationCategory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationCategory category;
    private String categoryLabel;
    private boolean isRead;
    private BigDecimal amount;
    private String receiptNumber;
    private LocalDateTime createdAt;
    private String timeAgo;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification entity) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setCategory(entity.getCategory());
        dto.setCategoryLabel(entity.getCategory() != null ? entity.getCategory().name() : "");
        dto.setRead(entity.isRead());
        dto.setAmount(entity.getAmount());
        dto.setReceiptNumber(entity.getReceiptNumber());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setTimeAgo(formatTimeAgo(entity.getCreatedAt()));
        return dto;
    }

    private static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days < 7) return days + "d ago";
        return days / 7 + "w ago";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
}
