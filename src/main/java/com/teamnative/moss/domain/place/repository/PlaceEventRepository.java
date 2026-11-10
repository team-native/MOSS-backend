package com.teamnative.moss.domain.place.repository;

import com.teamnative.moss.domain.place.entity.PlaceEvent;
import com.teamnative.moss.domain.place.entity.PlaceEventStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PlaceEventRepository extends JpaRepository<PlaceEvent, Long> {
    List<PlaceEvent> findByPlace_IdAndDeletedAtIsNullOrderByStartDateAscIdAsc(Long placeId);

    List<PlaceEvent> findByPlace_IdAndApprovalStatusAndDeletedAtIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscIdAsc(
            Long placeId, PlaceEventStatus status, LocalDate currentDateForStart, LocalDate currentDateForEnd);

    long countByPlace_IdAndApprovalStatusAndDeletedAtIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long placeId, PlaceEventStatus status, LocalDate currentDateForStart, LocalDate currentDateForEnd);

    long countByPlace_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long placeId, LocalDateTime from, LocalDateTime to);

    @Query("select count(distinct event.id) from PlaceEvent event where event.place.id = :placeId "
            + "and event.deletedAt is null and event.approvalStatus <> :rejected "
            + "and (:excludeId is null or event.id <> :excludeId) "
            + "and ((event.startDate <= :endDate and event.endDate >= :startDate) "
            + "or (event.pendingTitle is not null and event.pendingStartDate <= :endDate "
            + "and event.pendingEndDate >= :startDate))")
    long countOverlappingEvents(@Param("placeId") Long placeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("rejected") PlaceEventStatus rejected,
                                @Param("excludeId") Long excludeId);

    @Query("select event from PlaceEvent event where event.deletedAt is null and (event.approvalStatus = :pending "
            + "or event.pendingTitle is not null) order by event.createdAt asc")
    List<PlaceEvent> findPendingReviews(@Param("pending") PlaceEventStatus pending);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from PlaceEvent event where event.id = :id")
    Optional<PlaceEvent> findByIdForUpdate(@Param("id") Long id);

    Optional<PlaceEvent> findByIdAndPlace_Id(Long eventId, Long placeId);
}
