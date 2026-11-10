package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceEventReviewRequest;
import com.teamnative.moss.domain.place.dto.PlaceEventReviewResponse;
import com.teamnative.moss.domain.place.service.PlaceEventService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/place-events")
@Tag(name = "Admin Place Events")
public class AdminPlaceEventController {
    private final PlaceEventService eventService;

    public AdminPlaceEventController(PlaceEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "검토 대기 이벤트 목록", description = "신규 등록과 수정 재검토가 필요한 이벤트를 조회합니다.")
    public ApiResponse<List<PlaceEventReviewResponse>> pendingReviews(
            @AuthenticationPrincipal String email) {
        return ApiResponse.success("검토 대기 이벤트를 조회했습니다.", eventService.getPendingReviews(email));
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "이벤트 승인 또는 반려", description = "승인 시 노출하고 반려 시 업주에게 사유를 알립니다.")
    public ApiResponse<PlaceEventReviewResponse> review(
            @AuthenticationPrincipal String email,
            @PathVariable Long eventId,
            @Valid @RequestBody PlaceEventReviewRequest request) {
        return ApiResponse.success("이벤트 검토를 처리했습니다.", eventService.review(email, eventId, request));
    }
}
