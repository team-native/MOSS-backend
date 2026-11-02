package com.teamnative.moss.domain.review.controller;

import com.teamnative.moss.domain.review.service.ReviewService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "내가 쓴 리뷰 목록 조회", description = "TODO: 리뷰 기능 구현 후 로그인한 사용자가 작성한 리뷰 목록을 반환합니다.")
    public ApiResponse<List<Object>> myReviews(@AuthenticationPrincipal String email) {
        return ApiResponse.success("작성한 리뷰 목록을 조회했습니다.", reviewService.getMyReviews(email));
    }
}
