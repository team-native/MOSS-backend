package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.PlacePhoto;
import java.time.LocalDateTime;

public record PlacePhotoResponse(
        String id,
        String url,
        String originalFilename,
        String contentType,
        boolean representative,
        int sortOrder,
        LocalDateTime createdAt
) {
    public static PlacePhotoResponse from(PlacePhotoMetadata photo) {
        String url = "/places/%d/photos/%d".formatted(photo.placeId(), photo.id());
        return new PlacePhotoResponse(photo.id().toString(), url, photo.originalFilename(),
                photo.contentType(), photo.representative(), photo.sortOrder(), photo.createdAt());
    }

    public static PlacePhotoResponse from(PlacePhoto photo) {
        String url = "/places/%d/photos/%d".formatted(photo.getPlace().getId(), photo.getId());
        return new PlacePhotoResponse(photo.getId().toString(), url, photo.getOriginalFilename(),
                photo.getContentType(), photo.isRepresentative(), photo.getSortOrder(), photo.getCreatedAt());
    }
}
