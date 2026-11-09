package com.teamnative.moss.domain.main.dto;

import java.util.List;

public record NearbyPlaceResponse(double latitude, double longitude, int radiusMeters, List<Place> places) {
    public NearbyPlaceResponse {
        places = List.copyOf(places);
    }

    public record Place(
            String id,
            String name,
            String address,
            String type,
            double latitude,
            double longitude,
            int hourlyPrice,
            String noiseLevel,
            int distanceMeters
    ) {}
}
