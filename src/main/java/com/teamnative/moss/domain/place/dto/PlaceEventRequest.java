package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.EventTimeSlot;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record PlaceEventRequest(
        @NotBlank @Size(min = 2, max = 40) String title,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty List<@NotNull EventTimeSlot> targetTimeSlots,
        @NotBlank @Size(max = 500) String content
) {}
