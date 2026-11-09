package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.PlaceProduct;
import com.teamnative.moss.domain.place.entity.ProductStatus;
import java.time.LocalDateTime;

public record PlaceProductResponse(
        String id,
        String placeId,
        String name,
        int price,
        ProductStatus status,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaceProductResponse from(PlaceProduct product) {
        return new PlaceProductResponse(product.getId().toString(), product.getPlace().getId().toString(),
                product.getName(), product.getPrice(), product.getStatus(), product.getSortOrder(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
