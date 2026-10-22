package com.teamnative.moss.domain.auth.dto;

import java.time.LocalDateTime;

public record VerifyEmailResponse(String resetToken, LocalDateTime expiresAt) {
}
