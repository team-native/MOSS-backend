package com.teamnative.moss.domain.notification.controller;

import com.teamnative.moss.domain.notification.dto.NotificationListResponse;
import com.teamnative.moss.domain.notification.dto.NotificationResponse;
import com.teamnative.moss.domain.notification.service.NotificationService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림을 최신순으로 조회합니다.")
    public ApiResponse<NotificationListResponse> getNotifications(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ApiResponse.success("알림 목록을 조회했습니다.",
                notificationService.getNotifications(email, unreadOnly, page, size));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "로그인한 사용자의 알림을 읽음 처리합니다.")
    public ApiResponse<NotificationResponse> markRead(@AuthenticationPrincipal String email,
                                                       @PathVariable Long notificationId) {
        return ApiResponse.success("알림을 읽음 처리했습니다.",
                notificationService.markRead(email, notificationId));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 사용자의 미읽음 알림을 모두 읽음 처리합니다.")
    public ApiResponse<Integer> markAllRead(@AuthenticationPrincipal String email) {
        return ApiResponse.success("모든 알림을 읽음 처리했습니다.", notificationService.markAllRead(email));
    }
}
