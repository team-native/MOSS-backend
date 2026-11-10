package com.teamnative.moss.domain.place.entity;

import com.teamnative.moss.domain.search.entity.Place;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "place_events", indexes = {
        @Index(name = "idx_place_events_place_period", columnList = "place_id,start_date,end_date")
})
public class PlaceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ElementCollection
    @CollectionTable(name = "place_event_time_slots", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "slot_order")
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private List<EventTimeSlot> targetTimeSlots = new ArrayList<>();

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30) default 'APPROVED'")
    private PlaceEventStatus approvalStatus = PlaceEventStatus.APPROVED;

    @Column(name = "pending_title", length = 40)
    private String pendingTitle;

    @Column(name = "pending_start_date")
    private LocalDate pendingStartDate;

    @Column(name = "pending_end_date")
    private LocalDate pendingEndDate;

    @Column(name = "pending_target_time_slots", length = 100)
    private String pendingTargetTimeSlots;

    @Column(name = "pending_content", length = 500)
    private String pendingContent;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PlaceEvent() {}

    public PlaceEvent(Place place, String title, LocalDate startDate, LocalDate endDate,
                      List<EventTimeSlot> targetTimeSlots, String content, LocalDateTime now) {
        this.place = place;
        this.approvalStatus = PlaceEventStatus.PENDING_APPROVAL;
        update(title, startDate, endDate, targetTimeSlots, content, now);
        this.createdAt = now;
    }

    public void update(String title, LocalDate startDate, LocalDate endDate,
                       List<EventTimeSlot> targetTimeSlots, String content, LocalDateTime now) {
        if (approvalStatus == PlaceEventStatus.APPROVED) {
            this.pendingTitle = title;
            this.pendingStartDate = startDate;
            this.pendingEndDate = endDate;
            this.pendingTargetTimeSlots = encodeTimeSlots(targetTimeSlots);
            this.pendingContent = content;
            this.rejectionReason = null;
            this.updatedAt = now;
            return;
        }
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetTimeSlots.clear();
        this.targetTimeSlots.addAll(targetTimeSlots);
        this.content = content;
        this.updatedAt = now;
        this.approvalStatus = PlaceEventStatus.PENDING_APPROVAL;
        this.rejectionReason = null;
    }

    public void approve(LocalDateTime now) {
        if (approvalStatus != PlaceEventStatus.PENDING_APPROVAL && !hasPendingRevision()) {
            throw new IllegalStateException("검토 대기 중인 이벤트가 아닙니다.");
        }
        if (hasPendingRevision()) {
            this.title = pendingTitle;
            this.startDate = pendingStartDate;
            this.endDate = pendingEndDate;
            this.targetTimeSlots.clear();
            this.targetTimeSlots.addAll(decodeTimeSlots(pendingTargetTimeSlots));
            this.content = pendingContent;
            clearPendingRevision();
        }
        this.approvalStatus = PlaceEventStatus.APPROVED;
        this.rejectionReason = null;
        this.updatedAt = now;
    }

    public void reject(String reason, LocalDateTime now) {
        if (hasPendingRevision()) {
            clearPendingRevision();
        } else if (approvalStatus == PlaceEventStatus.PENDING_APPROVAL) {
            this.approvalStatus = PlaceEventStatus.REJECTED;
        } else {
            throw new IllegalStateException("검토 대기 중인 이벤트가 아닙니다.");
        }
        this.rejectionReason = reason;
        this.updatedAt = now;
    }

    public void softDelete(LocalDateTime now) {
        this.deletedAt = now;
        this.updatedAt = now;
        clearPendingRevision();
    }

    public boolean hasPendingRevision() {
        return pendingTitle != null;
    }

    public PlaceEventStatus getDisplayStatus(LocalDate today) {
        if (approvalStatus == PlaceEventStatus.APPROVED && endDate.isBefore(today)) {
            return PlaceEventStatus.ENDED;
        }
        return approvalStatus;
    }

    private void clearPendingRevision() {
        this.pendingTitle = null;
        this.pendingStartDate = null;
        this.pendingEndDate = null;
        this.pendingTargetTimeSlots = null;
        this.pendingContent = null;
    }

    private String encodeTimeSlots(List<EventTimeSlot> slots) {
        return slots.stream().map(Enum::name).collect(java.util.stream.Collectors.joining(","));
    }

    private List<EventTimeSlot> decodeTimeSlots(String slots) {
        return Arrays.stream(slots.split(",")).map(EventTimeSlot::valueOf).toList();
    }

    public Long getId() { return id; }
    public Place getPlace() { return place; }
    public String getTitle() { return title; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<EventTimeSlot> getTargetTimeSlots() { return List.copyOf(targetTimeSlots); }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public PlaceEventStatus getApprovalStatus() { return approvalStatus; }
    public String getPendingTitle() { return pendingTitle; }
    public LocalDate getPendingStartDate() { return pendingStartDate; }
    public LocalDate getPendingEndDate() { return pendingEndDate; }
    public List<EventTimeSlot> getPendingTargetTimeSlots() {
        return pendingTargetTimeSlots == null ? List.of() : decodeTimeSlots(pendingTargetTimeSlots);
    }
    public String getPendingContent() { return pendingContent; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
