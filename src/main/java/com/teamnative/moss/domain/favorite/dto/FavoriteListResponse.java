package com.teamnative.moss.domain.favorite.dto;

import java.util.List;

public record FavoriteListResponse(List<Place> places) {
    public FavoriteListResponse {
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
            boolean currentlyPrivate,
            int distanceMeters
    ) {}
}
