package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceEventRequest;
import com.teamnative.moss.domain.place.dto.PlaceEventResponse;
import com.teamnative.moss.domain.place.service.PlaceEventService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/places/{placeId}/events")
@Tag(name = "Business Place Events")
public class PlaceEventController {
    private final PlaceEventService eventService;

    public PlaceEventController(PlaceEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "업주 이벤트 목록 조회", description = "업주 매장의 전체 이벤트를 반환합니다.")
    public ApiResponse<List<PlaceEventResponse>> getEvents(
            @AuthenticationPrincipal String email, @PathVariable Long placeId) {
        return ApiResponse.success("이벤트 목록을 조회했습니다.", eventService.getBusinessEvents(email, placeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "이벤트 등록", description = "매장 이벤트를 등록합니다.")
    public ApiResponse<PlaceEventResponse> create(
            @AuthenticationPrincipal String email, @PathVariable Long placeId,
            @Valid @RequestBody PlaceEventRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "이벤트를 등록했습니다.",
                eventService.create(email, placeId, request));
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "이벤트 수정", description = "매장 이벤트 내용을 수정합니다.")
    public ApiResponse<PlaceEventResponse> update(
            @AuthenticationPrincipal String email, @PathVariable Long placeId, @PathVariable Long eventId,
            @Valid @RequestBody PlaceEventRequest request) {
        return ApiResponse.success("이벤트를 수정했습니다.", eventService.update(email, placeId, eventId, request));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "이벤트 삭제", description = "매장 이벤트를 삭제합니다.")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal String email, @PathVariable Long placeId, @PathVariable Long eventId) {
        eventService.delete(email, placeId, eventId);
        return ApiResponse.success("이벤트를 삭제했습니다.", null);
    }
}
