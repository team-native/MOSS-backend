package com.teamnative.moss.domain.main.controller;

import com.teamnative.moss.domain.main.dto.NearbyPlaceResponse;
import com.teamnative.moss.domain.main.service.MainService;
import com.teamnative.moss.global.dto.ApiResponse;
import com.teamnative.moss.global.exception.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/main")
@Tag(name = "Main")
public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/places")
    @Operation(summary = "주변 스터디카페와 카페 조회", description = "기준 위치 1km 이내 장소를 가까운 순으로 반환합니다.")
    public ApiResponse<NearbyPlaceResponse> nearbyPlaces(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double longitude,
            @RequestParam(defaultValue = "1000") @Min(200) @Max(1000) int radiusMeters
    ) {
        if (radiusMeters != 200 && radiusMeters != 500 && radiusMeters != 1_000) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "검색 반경은 200, 500, 1000미터 중 하나여야 합니다.");
        }
        return ApiResponse.success("주변 장소를 조회했습니다.",
                mainService.getNearbyPlaces(latitude, longitude, radiusMeters));
    }
}
