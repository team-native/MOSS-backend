package com.teamnative.moss.domain.notification.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationPushService {
    private final PushDeviceService pushDeviceService;
    private final FcmPushClient fcmPushClient;

    public NotificationPushService(PushDeviceService pushDeviceService, FcmPushClient fcmPushClient) {
        this.pushDeviceService = pushDeviceService;
        this.fcmPushClient = fcmPushClient;
    }

    public void send(Long userId, Long notificationId, String title, String message, String targetPath) {
        if (!fcmPushClient.isConfigured()) {
            return;
        }
        for (String token : pushDeviceService.tokensForUser(userId)) {
            FcmPushClient.SendResult result = fcmPushClient.send(token, title, message, notificationId, targetPath);
            if (result == FcmPushClient.SendResult.INVALID_TOKEN) {
                pushDeviceService.removeInvalidToken(token);
            }
        }
    }
}
