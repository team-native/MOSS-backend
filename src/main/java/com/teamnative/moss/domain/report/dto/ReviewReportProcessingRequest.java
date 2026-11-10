package com.teamnative.moss.domain.report.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record ReviewReportProcessingRequest(
        @NotNull Decision decision,
        @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.") String reason
) {
    public enum Decision {
        START_REVIEW,
        CONFIRM_VIOLATION,
        DISMISS
    }
}
