package com.teamnative.moss.domain.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import com.teamnative.moss.domain.search.entity.PreferredAgeGroup;

public record PlaceDetailResponse(
        String id,
        String name,
        String address,
        String type,
        double latitude,
        double longitude,
        String operatingHours,
        String contactNumber,
        String description,
        String holidays,
        int minimumHourlyPrice,
        int maximumHourlyPrice,
        String ageRestriction,
        Set<PreferredAgeGroup> preferredAgeGroups,
        String noiseLevel,
        boolean outlets,
        boolean wifi,
        boolean printer,
        boolean locker,
        boolean parking,
        boolean beverages,
        boolean studyRoom,
        boolean outsideFoodAllowed,
        String callAvailability,
        boolean laptopAllowed,
        BigDecimal averageRating,
        long reviewCount,
        List<PlaceEventResponse> events,
        NoiseDistribution noiseDistribution,
        Integer distanceMeters,
        boolean favorited,
        boolean currentlyPrivate,
        LocalDateTime informationUpdatedAt
) {
    public PlaceDetailResponse {
        preferredAgeGroups = Set.copyOf(preferredAgeGroups);
        events = List.copyOf(events);
    }

    public record NoiseDistribution(long quiet, long normal, long talk) {}
}
