package com.teamnative.moss.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlaceReviewListResponse(
        String placeId,
        long reviewCount,
        BigDecimal averageRating,
        NoiseDistribution noiseDistribution,
        List<Review> reviews,
        int page,
        int pageSize,
        boolean hasNext
) {
    public PlaceReviewListResponse {
        reviews = List.copyOf(reviews);
    }

    public record NoiseDistribution(long quiet, long normal, long talk) {}

    public record Review(
            String id,
            BigDecimal rating,
            String noiseLevel,
            String content,
            List<String> photoUrls,
            LocalDateTime createdAt
    ) {
        public Review {
            photoUrls = List.copyOf(photoUrls);
        }
    }
}
