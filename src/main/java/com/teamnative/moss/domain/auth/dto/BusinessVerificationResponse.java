package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.BusinessVerification;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BusinessVerificationResponse(
        String id,
        String accountName,
        String accountEmail,
        String registrationNumber,
        String representativeName,
        LocalDate openingDate,
        String documentName,
        String status,
        String reviewReason,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {
    public static BusinessVerificationResponse from(BusinessVerification verification) {
        return new BusinessVerificationResponse(verification.getId().toString(), verification.getUser().getName(),
                verification.getUser().getEmail(),
                verification.getRegistrationNumber(), verification.getRepresentativeName(),
                verification.getOpeningDate(), verification.getDocumentName(), verification.getStatus().name(),
                verification.getReviewReason(), verification.getSubmittedAt(), verification.getUpdatedAt());
    }

    public static BusinessVerificationResponse from(BusinessVerificationSummary summary) {
        return new BusinessVerificationResponse(summary.id().toString(), summary.accountName(), summary.accountEmail(),
                summary.registrationNumber(), summary.representativeName(), summary.openingDate(),
                summary.documentName(), summary.status().name(), summary.reviewReason(),
                summary.submittedAt(), summary.updatedAt());
    }
}
