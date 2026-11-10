package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceEventResponse;
import com.teamnative.moss.domain.place.service.PlaceEventService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/places/{placeId}/events")
@Tag(name = "Place Events")
public class PublicPlaceEventController {
    private final PlaceEventService eventService;

    public PublicPlaceEventController(PlaceEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "장소의 진행 중 이벤트 조회", description = "관리자 승인을 받고 진행 기간인 이벤트만 반환합니다.")
    public ApiResponse<List<PlaceEventResponse>> getActiveEvents(@PathVariable Long placeId) {
        return ApiResponse.success("진행 중인 이벤트를 조회했습니다.", eventService.getPublicActiveEvents(placeId));
    }
}
