package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.Grade;

public record ProfileUpdateResponse(String name, String email, Grade grade) {
}
