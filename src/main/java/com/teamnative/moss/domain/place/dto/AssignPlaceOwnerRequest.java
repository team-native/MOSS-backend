package com.teamnative.moss.domain.place.dto;

import jakarta.validation.constraints.NotNull;

public record AssignPlaceOwnerRequest(@NotNull Long businessUserId) {}
