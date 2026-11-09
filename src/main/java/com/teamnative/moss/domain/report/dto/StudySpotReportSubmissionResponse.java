package com.teamnative.moss.domain.report.dto;

public record StudySpotReportSubmissionResponse(
        boolean alreadyRegistered,
        String existingPlaceId,
        UserReportResponse report
) {}
