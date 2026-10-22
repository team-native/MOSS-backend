package com.teamnative.moss.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "사용자 ID")
        Long id,
        @Schema(description = "이름")
        String name,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "리프레시 토큰")
        String refreshToken,
        @Schema(description = "토큰 유형", example = "Bearer")
        String tokenType
) {
}
