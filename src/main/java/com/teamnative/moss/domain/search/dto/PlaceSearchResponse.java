package com.teamnative.moss.domain.search.dto;

import java.util.List;

public record PlaceSearchResponse(String query, List<Place> places) {
    public PlaceSearchResponse {
        places = List.copyOf(places);
    }

    public record Place(String id, String name, String address, double latitude, double longitude,
                        int hourlyPrice, String noiseLevel, String ageRestriction,
                        boolean callAllowed, boolean laptopAllowed, Integer distanceMeters) {}
}
