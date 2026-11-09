package com.teamnative.moss.domain.notification.dto;

import java.util.List;

public record NotificationListResponse(List<NotificationResponse> notifications, int page, int size,
                                       boolean hasNext, long unreadCount) {
    public NotificationListResponse {
        notifications = List.copyOf(notifications);
    }
}
