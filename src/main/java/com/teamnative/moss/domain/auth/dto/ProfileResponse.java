package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.Grade;

public record ProfileResponse(
        String name,
        String email,
        String number,
        Grade grade,
        long reviewCount,
        long reportCount,
        String schoolName,
        Double schoolLatitude,
        Double schoolLongitude
) {
}
