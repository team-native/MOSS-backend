package com.teamnative.moss.domain.place.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceEventReviewRequest(
        @NotNull Decision decision,
        @Size(max = 500) String reason
) {
    public enum Decision {
        APPROVE,
        REJECT
    }
}
