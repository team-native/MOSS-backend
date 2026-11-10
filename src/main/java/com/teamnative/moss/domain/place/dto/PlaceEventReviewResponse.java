package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.place.entity.PlaceEvent;
import com.teamnative.moss.domain.place.entity.EventTimeSlot;
import java.time.LocalDate;
import java.util.List;

public record PlaceEventReviewResponse(
        PlaceEventResponse current,
        String submittedTitle,
        LocalDate submittedStartDate,
        LocalDate submittedEndDate,
        List<EventTimeSlot> submittedTargetTimeSlots,
        String submittedContent,
        boolean revision
) {
    public PlaceEventReviewResponse {
        submittedTargetTimeSlots = List.copyOf(submittedTargetTimeSlots);
    }

    public static PlaceEventReviewResponse from(PlaceEvent event) {
        boolean revision = event.hasPendingRevision();
        return new PlaceEventReviewResponse(
                PlaceEventResponse.from(event),
                revision ? event.getPendingTitle() : event.getTitle(),
                revision ? event.getPendingStartDate() : event.getStartDate(),
                revision ? event.getPendingEndDate() : event.getEndDate(),
                revision ? event.getPendingTargetTimeSlots() : event.getTargetTimeSlots(),
                revision ? event.getPendingContent() : event.getContent(),
                revision
        );
    }
}
