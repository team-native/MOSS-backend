package com.teamnative.moss.domain.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SchoolPreferenceRequest(
        @NotBlank(message = "학교명을 입력해 주세요.")
        @Size(max = 100, message = "학교명은 100자 이하여야 합니다.")
        String schoolName,

        @NotNull(message = "학교 위도를 입력해 주세요.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
        Double latitude,

        @NotNull(message = "학교 경도를 입력해 주세요.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
        Double longitude
) {}
