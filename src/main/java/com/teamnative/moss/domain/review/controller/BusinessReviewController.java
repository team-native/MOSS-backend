package com.teamnative.moss.domain.review.controller;

import com.teamnative.moss.domain.review.dto.BusinessReplyRequest;
import com.teamnative.moss.domain.review.dto.BusinessReviewFilter;
import com.teamnative.moss.domain.review.dto.BusinessReviewListResponse;
import com.teamnative.moss.domain.review.service.BusinessReviewService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/business/reviews")
@Tag(name = "Business Reviews")
public class BusinessReviewController {
    private final BusinessReviewService businessReviewService;

    public BusinessReviewController(BusinessReviewService businessReviewService) {
        this.businessReviewService = businessReviewService;
    }

    @GetMapping
    @Operation(summary = "업주 매장 리뷰 조회", description = "업주가 매장 리뷰를 최신순으로 조회하고 답글 상태로 필터링합니다.")
    public ApiResponse<BusinessReviewListResponse> reviews(
            @AuthenticationPrincipal String email,
            @RequestParam Long placeId,
            @RequestParam(defaultValue = "ALL") BusinessReviewFilter filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success("매장 리뷰를 조회했습니다.",
                businessReviewService.getReviews(email, placeId, filter, page, size));
    }

    @PostMapping("/{reviewId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 답글 등록", description = "리뷰에 업주 답글을 등록합니다.")
    public ApiResponse<BusinessReviewListResponse.Reply> addReply(
            @AuthenticationPrincipal String email,
            @PathVariable Long reviewId,
            @Valid @RequestBody BusinessReplyRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "리뷰 답글을 등록했습니다.",
                businessReviewService.addReply(email, reviewId, request));
    }

    @PutMapping("/{reviewId}/reply")
    @Operation(summary = "리뷰 답글 수정", description = "답글 작성 업주가 답글을 수정합니다.")
    public ApiResponse<BusinessReviewListResponse.Reply> updateReply(
            @AuthenticationPrincipal String email,
            @PathVariable Long reviewId,
            @Valid @RequestBody BusinessReplyRequest request) {
        return ApiResponse.success("리뷰 답글을 수정했습니다.",
                businessReviewService.updateReply(email, reviewId, request));
    }

    @DeleteMapping("/{reviewId}/reply")
    @Operation(summary = "리뷰 답글 삭제", description = "답글 작성 업주가 답글을 삭제합니다.")
    public ApiResponse<Void> deleteReply(@AuthenticationPrincipal String email,
                                         @PathVariable Long reviewId) {
        businessReviewService.deleteReply(email, reviewId);
        return ApiResponse.success("리뷰 답글을 삭제했습니다.", null);
    }
}
