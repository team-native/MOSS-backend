package com.teamnative.moss.domain.auth.dto;

import java.time.LocalDate;

public record SignupResponse(
        Long userId,
        String name,
        String email,
        LocalDate createdAt
) {
}
