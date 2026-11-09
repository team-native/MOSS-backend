package com.teamnative.moss.domain.notification.dto;

import com.teamnative.moss.domain.notification.entity.Notification;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(Long id, NotificationType type, String title, String message,
                                   String targetPath, boolean read, LocalDateTime createdAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getTargetPath(), notification.getReadAt() != null,
                notification.getCreatedAt());
    }
}
