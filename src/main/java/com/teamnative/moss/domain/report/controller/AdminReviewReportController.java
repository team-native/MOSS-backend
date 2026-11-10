package com.teamnative.moss.domain.report.controller;

import com.teamnative.moss.domain.report.dto.ReviewReportAdminResponse;
import com.teamnative.moss.domain.report.dto.ReviewReportProcessingRequest;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.service.ReviewReportService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports/reviews")
@Tag(name = "Admin Review Reports")
public class AdminReviewReportController {
    private final ReviewReportService reviewReportService;

    public AdminReviewReportController(ReviewReportService reviewReportService) {
        this.reviewReportService = reviewReportService;
    }

    @GetMapping
    @Operation(summary = "리뷰 신고 검토 목록", description = "관리자가 신고된 리뷰와 처리 상태를 확인합니다.")
    public ApiResponse<List<ReviewReportAdminResponse>> getQueue(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) ReportStatus status) {
        return ApiResponse.success("리뷰 신고 목록을 조회했습니다.", reviewReportService.getQueue(email, status));
    }

    @PatchMapping("/{reportId}")
    @Operation(summary = "리뷰 신고 검토 처리", description = "신고 검토를 시작하거나 위반 여부를 결정하고 제재를 적용합니다.")
    public ApiResponse<ReviewReportAdminResponse> process(
            @AuthenticationPrincipal String email,
            @PathVariable Long reportId,
            @Valid @RequestBody ReviewReportProcessingRequest request) {
        return ApiResponse.success("리뷰 신고를 처리했습니다.",
                reviewReportService.process(email, reportId, request));
    }
}
