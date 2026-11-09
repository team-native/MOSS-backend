package com.teamnative.moss.domain.search.repository;

import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.entity.PlaceType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByNameContainingIgnoreCase(String name);

    Optional<Place> findFirstByNameIgnoreCaseAndAddressIgnoreCase(String name, String address);

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
