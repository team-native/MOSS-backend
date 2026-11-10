package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.EventTimeSlot;
import com.teamnative.moss.domain.place.entity.PlaceEvent;
import com.teamnative.moss.domain.place.entity.PlaceEventStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PlaceEventResponse(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        List<EventTimeSlot> targetTimeSlots,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PlaceEventStatus status,
        boolean revisionPending,
        String rejectionReason
) {
    public PlaceEventResponse {
        targetTimeSlots = List.copyOf(targetTimeSlots);
    }

    public static PlaceEventResponse from(PlaceEvent event) {
        return new PlaceEventResponse(event.getId(), event.getTitle(), event.getStartDate(), event.getEndDate(),
                event.getTargetTimeSlots(), event.getContent(), event.getCreatedAt(), event.getUpdatedAt(),
                event.getDisplayStatus(LocalDate.now()), event.hasPendingRevision(), event.getRejectionReason());
    }
}
