package com.teamnative.moss.domain.place.dto;

import java.util.List;

public record PlacePhotoListResponse(
        String placeId,
        boolean publiclyVisible,
        PlacePhotoResponse representativePhoto,
        List<PlacePhotoResponse> photos
) {
    public PlacePhotoListResponse {
        photos = List.copyOf(photos);
    }
}
