package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceProductRequest(
        @NotBlank @Size(max = 20) String name,
        @NotNull @Min(0) @Max(1_000_000) Integer price,
        @NotNull ProductStatus status,
        @Min(0) Integer sortOrder
) {}
