package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.report.entity.ReviewReportReason;
import jakarta.validation.constraints.Size;

public record ReviewReportRequest(
        @jakarta.validation.constraints.NotNull(message = "신고 사유를 선택해 주세요.")
        ReviewReportReason reason,

        @Size(max = 500, message = "상세 내용은 500자 이하여야 합니다.")
        String detail
) {}
