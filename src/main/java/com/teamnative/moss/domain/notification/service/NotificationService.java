package com.teamnative.moss.domain.notification.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.notification.dto.NotificationListResponse;
import com.teamnative.moss.domain.notification.dto.NotificationResponse;
import com.teamnative.moss.domain.notification.entity.Notification;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import com.teamnative.moss.domain.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
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

    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(String email, boolean unreadOnly, int page, int size) {
        UserAccount user = findUser(email);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> result = unreadOnly
                ? notificationRepository.findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(user.getId(), pageable)
                : notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable);
        return new NotificationListResponse(result.getContent().stream().map(NotificationResponse::from).toList(),
                page, size, result.hasNext(), notificationRepository.countByUser_IdAndReadAtIsNull(user.getId()));
    }

    @Transactional
    public NotificationResponse markRead(String email, Long notificationId) {
        UserAccount user = findUser(email);
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.markRead(LocalDateTime.now());
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String email) {
        UserAccount user = findUser(email);
        return notificationRepository.markAllRead(user.getId(), LocalDateTime.now());
    }

    @Transactional
    public void create(UserAccount user, NotificationType type, String title, String message, String targetPath) {
        notificationRepository.save(new Notification(user, type, title, message, targetPath, LocalDateTime.now()));
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }
}
