package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceDetailResponse;
import com.teamnative.moss.domain.place.service.PlaceDetailService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/places")
@Tag(name = "Place")
public class PlaceDetailController {
    private final PlaceDetailService placeDetailService;

    public PlaceDetailController(PlaceDetailService placeDetailService) {
        this.placeDetailService = placeDetailService;
    }

    @GetMapping("/{placeId}")
    @Operation(summary = "장소 상세 조회", description = "장소 정보, 리뷰 집계, 거리와 찜 상태를 반환합니다.")
    public ApiResponse<PlaceDetailResponse> getPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @AuthenticationPrincipal String email
    ) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("거리 계산에는 latitude와 longitude가 모두 필요합니다.");
        }
        return ApiResponse.success("장소 상세를 조회했습니다.",
                placeDetailService.getPlaceDetail(placeId, latitude, longitude, email));
    }
}
