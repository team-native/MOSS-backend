package com.teamnative.moss.domain.notification.dto;

import com.teamnative.moss.domain.notification.entity.PushPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PushDeviceRequest(
        @NotBlank @Size(max = 4096) String token,
        @NotNull PushPlatform platform
) {}
