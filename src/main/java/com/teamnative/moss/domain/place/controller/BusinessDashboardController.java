package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.BusinessDashboardResponse;
import com.teamnative.moss.domain.place.service.BusinessDashboardService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/places/{placeId}/dashboard")
@Tag(name = "Business Dashboard")
public class BusinessDashboardController {
    private final BusinessDashboardService dashboardService;

    public BusinessDashboardController(BusinessDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "업주 대시보드 조회", description = "승인된 업주에게 매장 현황, 리뷰, 정보 오류 제보 요약을 제공합니다.")
    public ApiResponse<BusinessDashboardResponse> getDashboard(
            @AuthenticationPrincipal String email, @PathVariable Long placeId) {
        return ApiResponse.success("업주 대시보드를 조회했습니다.", dashboardService.getDashboard(email, placeId));
    }
}
