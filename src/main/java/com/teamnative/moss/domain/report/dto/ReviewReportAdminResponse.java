package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.report.entity.ReviewReportOutcome;
import com.teamnative.moss.domain.report.entity.ReviewSanctionLevel;
import com.teamnative.moss.domain.report.entity.UserReport;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewReportAdminResponse(
        String id,
        String reportNumber,
        String status,
        String placeId,
        String placeName,
        String reviewId,
        String reviewAuthorId,
        String reviewContent,
        List<String> reviewPhotoUrls,
        String reason,
        String detail,
        ReviewReportOutcome outcome,
        ReviewSanctionLevel sanctionLevel,
        LocalDateTime sanctionUntil,
        String resultReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ReviewReportAdminResponse {
        reviewPhotoUrls = List.copyOf(reviewPhotoUrls);
    }

    public static ReviewReportAdminResponse from(UserReport report) {
        var review = report.getReview();
        return new ReviewReportAdminResponse(
                report.getId().toString(), report.getReportNumber(), report.getStatus().name(),
                report.getPlace() == null ? null : report.getPlace().getId().toString(),
                report.getPlaceName(), review == null ? null : review.getId().toString(),
                review == null ? null : review.getAuthor().getId().toString(),
                review == null ? null : review.getContent(),
                review == null ? List.of() : review.getPhotoUrls(),
                report.getReviewReportReason() == null ? null : report.getReviewReportReason().name(),
                report.getContent(), report.getReviewReportOutcome(), report.getReviewSanctionLevel(),
                report.getReviewSanctionUntil(), report.getResultReason(), report.getCreatedAt(), report.getUpdatedAt()
        );
    }
}
