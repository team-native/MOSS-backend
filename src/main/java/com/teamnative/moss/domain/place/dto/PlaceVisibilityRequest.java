package com.teamnative.moss.domain.place.dto;

import jakarta.validation.constraints.NotNull;

public record PlaceVisibilityRequest(@NotNull Boolean publiclyVisible) {}
