package com.teamnative.moss.domain.favorite.dto;

import java.time.LocalDateTime;

public record FavoriteMutationResponse(
        String placeId,
        boolean favorited,
        long activeFavoriteCount,
        LocalDateTime undoExpiresAt
) {}
