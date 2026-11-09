package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.report.entity.UserReport;
import java.time.LocalDateTime;
import java.util.List;

public record UserReportResponse(
        String id,
        String reportNumber,
        String reportType,
        String status,
        String placeName,
        String address,
        String placeType,
        String recommendationReason,
        List<String> photoUrls,
        String resultReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserReportResponse {
        photoUrls = List.copyOf(photoUrls);
    }

    public static UserReportResponse from(UserReport report) {
        return new UserReportResponse(report.getId().toString(), report.getReportNumber(), report.getReportType().name(),
                report.getStatus().name(), report.getPlaceName(), report.getAddress(),
                report.getPlaceType() == null ? null : report.getPlaceType().name(),
                report.getRecommendationReason(), report.getPhotoUrls(), report.getResultReason(),
                report.getCreatedAt(), report.getUpdatedAt());
    }
}
