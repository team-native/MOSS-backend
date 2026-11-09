package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BusinessVerificationSummary(
        Long id,
        String accountName,
        String accountEmail,
        String registrationNumber,
        String representativeName,
        LocalDate openingDate,
        String documentName,
        BusinessVerificationStatus status,
        String reviewReason,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {}
