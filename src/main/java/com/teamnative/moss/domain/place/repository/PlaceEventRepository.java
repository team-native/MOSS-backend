package com.teamnative.moss.domain.place.repository;

import com.teamnative.moss.domain.place.entity.PlaceEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceEventRepository extends JpaRepository<PlaceEvent, Long> {
    List<PlaceEvent> findByPlace_IdOrderByStartDateAscIdAsc(Long placeId);

    List<PlaceEvent> findByPlace_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscIdAsc(
            Long placeId, LocalDate currentDateForStart, LocalDate currentDateForEnd);

    long countByPlace_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long placeId, LocalDate currentDateForStart, LocalDate currentDateForEnd);

    Optional<PlaceEvent> findByIdAndPlace_Id(Long eventId, Long placeId);
}
