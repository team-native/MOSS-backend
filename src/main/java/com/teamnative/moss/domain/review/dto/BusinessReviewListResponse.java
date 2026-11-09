package com.teamnative.moss.domain.review.dto;

import com.teamnative.moss.domain.review.entity.PlaceReview;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BusinessReviewListResponse(
        String placeId,
        List<Review> reviews,
        int page,
        int pageSize,
        boolean hasNext
) {
    public BusinessReviewListResponse {
        reviews = List.copyOf(reviews);
    }

    public record Review(
            String id,
            String placeName,
            BigDecimal rating,
            String noiseLevel,
            String content,
            List<String> photoUrls,
            LocalDateTime createdAt,
            Reply reply
    ) {
        public Review {
            photoUrls = List.copyOf(photoUrls);
        }

        public static Review from(PlaceReview review) {
            Reply reply = review.getBusinessReply() == null ? null : new Reply(
                    review.getBusinessReply(),
                    review.getBusinessReplyCreatedAt(), review.getBusinessReplyUpdatedAt(),
                    review.getBusinessReplyCreatedAt().plusDays(3));
            return new Review(review.getId().toString(), review.getPlace().getName(), review.getRating(),
                    review.getNoiseLevel().name(), review.getContent(), review.getPhotoUrls(),
                    review.getCreatedAt(), reply);
        }
    }

    public record Reply(String content, LocalDateTime createdAt,
                        LocalDateTime updatedAt, LocalDateTime editableUntil) {}
}
