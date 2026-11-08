package com.teamnative.moss.domain.review.controller;

import com.teamnative.moss.domain.review.service.ReviewService;
import com.teamnative.moss.domain.review.dto.ReviewRequest;
import com.teamnative.moss.domain.review.dto.ReviewResponse;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@Tag(name = "Review")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/my")
    @Operation(summary = "내가 쓴 리뷰 목록 조회", description = "로그인한 학생이 작성한 리뷰 목록을 최신순으로 반환합니다.")
    public ApiResponse<List<ReviewResponse>> myReviews(@AuthenticationPrincipal String email) {
        return ApiResponse.success("작성한 리뷰 목록을 조회했습니다.", reviewService.getMyReviews(email));
    }

    @PostMapping("/places/{placeId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "장소 리뷰 작성", description = "별점, 체감 소음, 리뷰 내용과 사진 참조를 저장합니다.")
    public ApiResponse<ReviewResponse> createReview(@AuthenticationPrincipal String email,
                                                    @PathVariable Long placeId,
                                                    @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "리뷰를 작성했습니다.",
                reviewService.createReview(email, placeId, request));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 수정", description = "본인 리뷰를 수정합니다. 다음 수정은 24시간 후 가능합니다.")
    public ApiResponse<ReviewResponse> updateReview(@AuthenticationPrincipal String email,
                                                    @PathVariable Long reviewId,
                                                    @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.success("리뷰를 수정했습니다.", reviewService.updateReview(email, reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 삭제", description = "삭제한 리뷰는 7일 동안 복구할 수 있습니다.")
    public ApiResponse<ReviewResponse> deleteReview(@AuthenticationPrincipal String email,
                                                   @PathVariable Long reviewId) {
        return ApiResponse.success("리뷰를 삭제했습니다.", reviewService.deleteReview(email, reviewId));
    }

    @PostMapping("/{reviewId}/restore")
    @Operation(summary = "삭제한 리뷰 복구", description = "삭제 후 7일 이내에 본인 리뷰를 복구합니다.")
    public ApiResponse<ReviewResponse> restoreReview(@AuthenticationPrincipal String email,
                                                    @PathVariable Long reviewId) {
        return ApiResponse.success("리뷰를 복구했습니다.", reviewService.restoreReview(email, reviewId));
    }
}
