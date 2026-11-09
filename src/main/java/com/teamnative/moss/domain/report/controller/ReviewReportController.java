package com.teamnative.moss.domain.report.controller;

import com.teamnative.moss.domain.report.dto.ReviewReportRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.service.ReviewReportService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewReportController {
    private final ReviewReportService reviewReportService;

    public ReviewReportController(ReviewReportService reviewReportService) {
        this.reviewReportService = reviewReportService;
    }

    @PostMapping("/review/{reviewId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 신고", description = "신고 사유와 선택 상세 내용을 접수합니다. 신고만으로 리뷰를 숨기거나 삭제하지 않습니다.")
    public ApiResponse<UserReportResponse> reportReview(@AuthenticationPrincipal String email,
                                                        @PathVariable Long reviewId,
                                                        @Valid @RequestBody ReviewReportRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "리뷰 신고가 접수되었습니다.",
                reviewReportService.submit(email, reviewId, request));
    }
}
