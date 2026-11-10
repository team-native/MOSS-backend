package com.teamnative.moss.domain.notification.service;

import com.teamnative.moss.domain.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationRetentionService {
    private final NotificationRepository notificationRepository;

    public NotificationRetentionService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 45 3 * * *")
    @Transactional
    public int deleteExpiredNotifications() {
        return notificationRepository.deleteOlderThan(LocalDateTime.now().minusDays(30));
    }
}
