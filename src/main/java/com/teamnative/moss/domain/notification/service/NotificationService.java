package com.teamnative.moss.domain.notification.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.notification.dto.NotificationListResponse;
import com.teamnative.moss.domain.notification.dto.NotificationResponse;
import com.teamnative.moss.domain.notification.entity.Notification;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import com.teamnative.moss.domain.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPushService notificationPushService;

    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository,
                               NotificationPushService notificationPushService) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.notificationPushService = notificationPushService;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(String email, boolean unreadOnly, int page, int size) {
        UserAccount user = findUser(email);
        PageRequest pageable = PageRequest.of(page, size);
        LocalDateTime retentionCutoff = LocalDateTime.now().minusDays(30);
        Page<Notification> result = unreadOnly
                ? notificationRepository.findByUser_IdAndReadAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        user.getId(), retentionCutoff, pageable)
                : notificationRepository.findByUser_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        user.getId(), retentionCutoff, pageable);
        return new NotificationListResponse(result.getContent().stream().map(NotificationResponse::from).toList(),
                page, size, result.hasNext(), notificationRepository
                .countByUser_IdAndReadAtIsNullAndCreatedAtGreaterThanEqual(user.getId(), retentionCutoff));
    }

    @Transactional
    public NotificationResponse markRead(String email, Long notificationId) {
        UserAccount user = findUser(email);
        Notification notification = notificationRepository.findByIdAndUser_IdAndCreatedAtGreaterThanEqual(
                        notificationId, user.getId(), LocalDateTime.now().minusDays(30))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.markRead(LocalDateTime.now());
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String email) {
        UserAccount user = findUser(email);
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.markAllRead(user.getId(), now.minusDays(30), now);
    }

    @Transactional
    public void create(UserAccount user, NotificationType type, String title, String message, String targetPath) {
        Notification notification = notificationRepository.saveAndFlush(
                new Notification(user, type, title, message, targetPath, LocalDateTime.now()));
        Long userId = user.getId();
        Long notificationId = notification.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationPushService.send(userId, notificationId, title, message, targetPath);
            }
        });
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }
}
