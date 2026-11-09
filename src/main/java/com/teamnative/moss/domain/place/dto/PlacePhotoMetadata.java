package com.teamnative.moss.domain.place.dto;

import java.time.LocalDateTime;

public record PlacePhotoMetadata(Long id, Long placeId, String originalFilename, String contentType,
                                 boolean representative, int sortOrder, LocalDateTime createdAt) {}
