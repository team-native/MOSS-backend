package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.favorite.repository.FavoriteRepository;
import com.teamnative.moss.domain.place.dto.PlaceDetailResponse;
import com.teamnative.moss.domain.place.dto.PlaceEventResponse;
import com.teamnative.moss.domain.place.entity.PlaceEventStatus;
import com.teamnative.moss.domain.place.repository.PlaceEventRepository;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaceDetailService {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final PlaceRepository placeRepository;
    private final PlaceReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlaceEventRepository eventRepository;

    public PlaceDetailService(PlaceRepository placeRepository, PlaceReviewRepository reviewRepository,
                              FavoriteRepository favoriteRepository, UserRepository userRepository,
                              PlaceEventRepository eventRepository) {
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId, Double latitude, Double longitude, String email) {
        Place place = placeRepository.findById(placeId)
                .filter(Place::isPubliclyVisible)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다."));

        long reviewCount = reviewRepository.countByPlace_IdAndDeletedAtIsNull(placeId);
        Double averageValue = reviewRepository.findAverageRating(placeId);
        BigDecimal averageRating = averageValue == null ? null
                : BigDecimal.valueOf(averageValue).setScale(2, RoundingMode.HALF_UP);

        Map<PlaceSearchFilter.NoiseLevel, Long> distributionCounts = new EnumMap<>(PlaceSearchFilter.NoiseLevel.class);
        for (Object[] row : reviewRepository.countNoiseLevels(placeId)) {
            distributionCounts.put((PlaceSearchFilter.NoiseLevel) row[0], ((Number) row[1]).longValue());
        }
        PlaceDetailResponse.NoiseDistribution noiseDistribution = new PlaceDetailResponse.NoiseDistribution(
                distributionCounts.getOrDefault(PlaceSearchFilter.NoiseLevel.QUIET, 0L),
                distributionCounts.getOrDefault(PlaceSearchFilter.NoiseLevel.NORMAL, 0L),
                distributionCounts.getOrDefault(PlaceSearchFilter.NoiseLevel.TALK, 0L));

        String noiseLevel = reviewCount >= 5 ? majorityNoiseOrDefault(distributionCounts, place) : place.getNoiseLevel().name();
        Integer distance = latitude == null ? null
                : (int) Math.round(distanceMeters(latitude, longitude, place.getLatitude(), place.getLongitude()));
        boolean favorited = false;
        if (email != null) {
            UserAccount user = userRepository.findByEmail(email).orElse(null);
            favorited = user != null && favoriteRepository.existsByUser_IdAndPlace_IdAndRemovedAtIsNull(
                    user.getId(), placeId);
        }

        return new PlaceDetailResponse(place.getId().toString(), place.getName(), place.getAddress(),
                place.getPlaceType().name(), place.getLatitude(), place.getLongitude(), place.getOperatingHours(),
                place.getContactNumber(), place.getDescription(), place.getHolidays(),
                place.getHourlyPrice(), place.getMaxHourlyPrice(), place.getAgeRestriction().name(),
                place.getPreferredAgeGroups(), noiseLevel,
                place.hasOutlets(), place.hasWifi(), place.hasPrinter(), place.hasLocker(), place.hasParking(),
                place.providesBeverages(), place.hasStudyRoom(), place.isOutsideFoodAllowed(),
                callAvailability(place), place.isLaptopAllowed(), averageRating,
                reviewCount, getActiveEvents(placeId), noiseDistribution, distance, favorited, false,
                place.getInformationUpdatedAt());
    }

    private java.util.List<PlaceEventResponse> getActiveEvents(Long placeId) {
        LocalDate today = LocalDate.now();
        return eventRepository.findByPlace_IdAndApprovalStatusAndDeletedAtIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscIdAsc(
                        placeId, PlaceEventStatus.APPROVED, today, today)
                .stream().map(PlaceEventResponse::from).toList();
    }

    private String majorityNoiseOrDefault(Map<PlaceSearchFilter.NoiseLevel, Long> counts, Place place) {
        long quiet = counts.getOrDefault(PlaceSearchFilter.NoiseLevel.QUIET, 0L);
        long normal = counts.getOrDefault(PlaceSearchFilter.NoiseLevel.NORMAL, 0L);
        long talk = counts.getOrDefault(PlaceSearchFilter.NoiseLevel.TALK, 0L);
        long max = Math.max(quiet, Math.max(normal, talk));
        int leaders = (quiet == max ? 1 : 0) + (normal == max ? 1 : 0) + (talk == max ? 1 : 0);
        if (leaders != 1) return place.getNoiseLevel().name();
        if (quiet == max) return PlaceSearchFilter.NoiseLevel.QUIET.name();
        if (normal == max) return PlaceSearchFilter.NoiseLevel.NORMAL.name();
        return PlaceSearchFilter.NoiseLevel.TALK.name();
    }

    private String callAvailability(Place place) {
        if (place.isCallRoomOnly()) return "ROOM_ONLY";
        return place.isCallAllowed() ? "ALLOWED" : "NOT_ALLOWED";
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
