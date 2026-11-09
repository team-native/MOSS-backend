package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.report.entity.ReportStatus;
import jakarta.validation.constraints.Size;

public record ReportProcessingRequest(
        @jakarta.validation.constraints.NotNull(message = "처리 상태를 선택해 주세요.")
        ReportStatus status,

        @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
        String reason
) {}
