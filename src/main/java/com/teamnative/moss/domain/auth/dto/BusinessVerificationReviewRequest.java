package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusinessVerificationReviewRequest(
        @NotNull(message = "처리 상태를 선택해 주세요.")
        BusinessVerificationStatus status,

        @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
        String reason
) {}
