package com.teamnative.moss.domain.main.service;

import com.teamnative.moss.domain.main.dto.NearbyPlaceResponse;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.entity.PlaceType;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MainService {
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final List<PlaceType> DISPLAYED_TYPES = List.of(PlaceType.STUDY_CAFE, PlaceType.CAFE);

    private final PlaceRepository placeRepository;

    public MainService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Transactional(readOnly = true)
    public NearbyPlaceResponse getNearbyPlaces(double latitude, double longitude, int radiusMeters) {
        double latitudeDelta = radiusMeters / 111_320.0;
        double minLatitude = Math.max(-90, latitude - latitudeDelta);
        double maxLatitude = Math.min(90, latitude + latitudeDelta);
        double longitudeDelta = radiusMeters / (111_320.0 * Math.max(Math.cos(Math.toRadians(latitude)), 0.0001));

        List<Place> candidates;
        if (longitudeDelta >= 180) {
            candidates = placeRepository.findByPlaceTypeInAndLatitudeBetween(
                    DISPLAYED_TYPES, minLatitude, maxLatitude);
        } else {
            double minLongitude = longitude - longitudeDelta;
            double maxLongitude = longitude + longitudeDelta;
            if (minLongitude < -180) {
                candidates = placeRepository.findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
                        DISPLAYED_TYPES, minLatitude, maxLatitude, minLongitude + 360, 180);
                candidates.addAll(placeRepository.findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
                        DISPLAYED_TYPES, minLatitude, maxLatitude, -180, maxLongitude));
            } else if (maxLongitude > 180) {
                candidates = placeRepository.findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
                        DISPLAYED_TYPES, minLatitude, maxLatitude, minLongitude, 180);
                candidates.addAll(placeRepository.findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
                        DISPLAYED_TYPES, minLatitude, maxLatitude, -180, maxLongitude - 360));
            } else {
                candidates = placeRepository.findByPlaceTypeInAndLatitudeBetweenAndLongitudeBetween(
                        DISPLAYED_TYPES, minLatitude, maxLatitude, minLongitude, maxLongitude);
            }
        }

        List<NearbyPlaceResponse.Place> places = candidates.stream()
                .map(place -> toResponse(place, latitude, longitude))
                .filter(place -> place.distanceMeters() <= radiusMeters)
                .sorted(Comparator.comparingInt(NearbyPlaceResponse.Place::distanceMeters))
                .toList();
        return new NearbyPlaceResponse(latitude, longitude, radiusMeters, places);
    }

    private NearbyPlaceResponse.Place toResponse(Place place, double latitude, double longitude) {
        int distance = (int) Math.round(distanceMeters(latitude, longitude,
                place.getLatitude(), place.getLongitude()));
        return new NearbyPlaceResponse.Place(
                place.getId().toString(), place.getName(), place.getAddress(), place.getPlaceType().name(),
                place.getLatitude(), place.getLongitude(), place.getHourlyPrice(), place.getNoiseLevel().name(), distance);
    }

    private static double distanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double latDelta = Math.toRadians(latitude2 - latitude1);
        double lonDelta = Math.toRadians(longitude2 - longitude1);
        double sinLat = Math.sin(latDelta / 2);
        double sinLon = Math.sin(lonDelta / 2);
        double a = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
