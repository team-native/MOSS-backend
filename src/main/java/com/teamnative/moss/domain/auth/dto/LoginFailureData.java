package com.teamnative.moss.domain.auth.dto;

public record LoginFailureData(int failedAttempts, int maxAttempts) {
}
