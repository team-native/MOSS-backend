package com.teamnative.moss.domain.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceDetailResponse(
        String id,
        String name,
        String address,
        String type,
        double latitude,
        double longitude,
        String operatingHours,
        int minimumHourlyPrice,
        int maximumHourlyPrice,
        String ageRestriction,
        String noiseLevel,
        boolean outlets,
        boolean wifi,
        String callAvailability,
        boolean laptopAllowed,
        BigDecimal averageRating,
        long reviewCount,
        NoiseDistribution noiseDistribution,
        Integer distanceMeters,
        boolean favorited,
        boolean currentlyPrivate,
        LocalDateTime informationUpdatedAt
) {
    public record NoiseDistribution(long quiet, long normal, long talk) {}
}
