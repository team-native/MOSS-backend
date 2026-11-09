package com.teamnative.moss.domain.review.dto;

import com.teamnative.moss.domain.review.entity.PlaceReview;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        String id,
        String placeId,
        String placeName,
        BigDecimal rating,
        String noiseLevel,
        String content,
        List<String> photoUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        LocalDateTime restoreUntil
) {
    public ReviewResponse {
        photoUrls = List.copyOf(photoUrls);
    }

    public static ReviewResponse from(PlaceReview review) {
        return new ReviewResponse(review.getId().toString(), review.getPlace().getId().toString(),
                review.getPlace().getName(), review.getRating(), review.getNoiseLevel().name(), review.getContent(),
                review.getPhotoUrls(), review.getCreatedAt(), review.getUpdatedAt(), review.getDeletedAt(),
                review.getRestoreUntil());
    }
}
