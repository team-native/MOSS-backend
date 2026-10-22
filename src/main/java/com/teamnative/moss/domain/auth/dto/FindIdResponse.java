package com.teamnative.moss.domain.auth.dto;

import java.time.LocalDate;

public record FindIdResponse(String email, LocalDate createdAt) {
}
