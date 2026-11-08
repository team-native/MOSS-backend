package com.teamnative.moss.domain.search.repository;

import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.entity.PlaceType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByNameContainingIgnoreCase(String name);

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
