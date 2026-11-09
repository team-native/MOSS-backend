package com.teamnative.moss.domain.search.repository;

import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.entity.PlaceType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByNameContainingIgnoreCase(String name);

    Optional<Place> findFirstByNameIgnoreCaseAndAddressIgnoreCase(String name, String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select place from Place place where place.id = :id")
    Optional<Place> findByIdForReport(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select place from Place place where place.id = :id")
    Optional<Place> findByIdForInformationUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select place from Place place where place.id = :id")
    Optional<Place> findByIdForVisibilityUpdate(@Param("id") Long id);

    List<Place> findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
            Collection<PlaceType> placeTypes,
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude
    );

    List<Place> findByPlaceTypeInAndLatitudeBetween(
            Collection<PlaceType> placeTypes,
            double minLatitude,
            double maxLatitude
    );
}
