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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PlaceEvent() {}

    public PlaceEvent(Place place, String title, LocalDate startDate, LocalDate endDate,
                      List<EventTimeSlot> targetTimeSlots, String content, LocalDateTime now) {
        this.place = place;
        update(title, startDate, endDate, targetTimeSlots, content, now);
        this.createdAt = now;
    }

    public void update(String title, LocalDate startDate, LocalDate endDate,
                       List<EventTimeSlot> targetTimeSlots, String content, LocalDateTime now) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetTimeSlots.clear();
        this.targetTimeSlots.addAll(targetTimeSlots);
        this.content = content;
        this.updatedAt = now;
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
}
