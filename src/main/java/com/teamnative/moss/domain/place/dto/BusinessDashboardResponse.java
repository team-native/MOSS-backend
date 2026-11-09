package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.report.entity.InfoIssueType;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.search.entity.Place;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BusinessDashboardResponse(
        Long placeId,
        String placeName,
        boolean publiclyVisible,
        LocalDateTime informationUpdatedAt,
        long totalReviewCount,
        long recentSevenDayReviewCount,
        long previousSevenDayReviewCount,
        long reviewCountChange,
        long unansweredReviewCount,
        Long activeEventCount,
        long pendingInfoErrorReportCount,
        List<InfoErrorReport> recentInfoErrorReports,
        List<RecentReview> recentReviews
) {
    public static BusinessDashboardResponse from(Place place, long totalReviews,
            long recentReviewsCount, long previousReviewsCount, long unansweredReviews,
            long activeEvents, long pendingInfoReports, List<UserReport> infoReports,
            List<PlaceReview> reviews) {
        return new BusinessDashboardResponse(
                place.getId(), place.getName(), place.isPubliclyVisible(), place.getInformationUpdatedAt(),
                totalReviews, recentReviewsCount, previousReviewsCount,
                recentReviewsCount - previousReviewsCount, unansweredReviews,
                activeEvents, pendingInfoReports,
                infoReports.stream().map(InfoErrorReport::from).toList(),
                reviews.stream().map(RecentReview::from).toList());
    }

    public record InfoErrorReport(Long reportId, InfoIssueType issueType, String content,
                                  ReportStatus status, LocalDateTime createdAt) {
        static InfoErrorReport from(UserReport report) {
            return new InfoErrorReport(report.getId(), report.getIssueType(), report.getContent(),
                    report.getStatus(), report.getCreatedAt());
        }
    }

    public record RecentReview(Long reviewId, BigDecimal rating, String content,
                               LocalDateTime createdAt, boolean answered) {
        static RecentReview from(PlaceReview review) {
            return new RecentReview(review.getId(), review.getRating(), review.getContent(),
                    review.getCreatedAt(), review.getBusinessReply() != null);
        }
    }
}
