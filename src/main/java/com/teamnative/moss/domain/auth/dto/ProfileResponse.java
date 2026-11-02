package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.Grade;

public record ProfileResponse(
        String name,
        String email,
        Grade grade,
        long reviewCount,
        long reportCount
) {
}
