package com.teamnative.moss.domain.search.service;

import com.teamnative.moss.domain.search.dto.PlaceSearchResponse;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import com.teamnative.moss.global.exception.DomainException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private final PlaceRepository placeRepository;
    private final RecentSearchService recentSearchService;

    public SearchService(PlaceRepository placeRepository, RecentSearchService recentSearchService) {
        this.placeRepository = placeRepository;
        this.recentSearchService = recentSearchService;
    }

    public List<com.teamnative.moss.domain.search.dto.RecentSearchResponse> getRecentSearches(String email) {
        return recentSearchService.getRecentSearches(email);
    }

    public void deleteRecentSearch(String email, Long id) {
        recentSearchService.deleteRecentSearch(email, id);
    }

    public void deleteAllRecentSearches(String email) {
        recentSearchService.deleteAllRecentSearches(email);
    }

    public PlaceSearchResponse searchPlaces(String email, String rawQuery, PlaceSearchFilter filter) {
        String query = normalize(rawQuery);
        if (email != null) {
            recentSearchService.record(email, query);
        }
        String normalizedQuery = query.toLowerCase(java.util.Locale.ROOT);
        Comparator<PlaceSearchResponse.Place> comparator = switch (filter.sort()) {
            case PRICE_ASC -> Comparator.comparingInt(PlaceSearchResponse.Place::hourlyPrice)
                    .thenComparing(PlaceSearchResponse.Place::id);
            case QUIET -> Comparator.<PlaceSearchResponse.Place>comparingInt(place -> quietOrder(place.noiseLevel()))
                    .thenComparing(PlaceSearchResponse.Place::id);
            case DISTANCE -> Comparator.comparing(PlaceSearchResponse.Place::distanceMeters,
                            Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(PlaceSearchResponse.Place::id);
        };
        List<PlaceSearchResponse.Place> places = placeRepository.findAll()
                .stream()
                .filter(Place::isPubliclyVisible)
                .filter(place -> normalize(place.getName()).toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery))
                .filter(place -> matches(place, filter))
                .map(place -> toResponse(place, filter))
                .sorted(comparator)
                .toList();
        return new PlaceSearchResponse(query, places);
    }

    private int quietOrder(String noiseLevel) {
        return switch (noiseLevel) {
            case "QUIET" -> 0;
            case "NORMAL" -> 1;
            case "TALK" -> 2;
            default -> 3;
        };
    }

    private boolean matches(Place place, PlaceSearchFilter filter) {
        if (filter.price() != null && place.getHourlyPrice() > filter.price()) return false;
        if (filter.minPrice() != null && place.getHourlyPrice() < filter.minPrice()) return false;
        if (filter.maxPrice() != null && place.getHourlyPrice() > filter.maxPrice()) return false;
        if (filter.noiseLevel() != null && place.getNoiseLevel() != filter.noiseLevel()) return false;
        if (filter.ageRestriction() != null && place.getAgeRestriction() != filter.ageRestriction()) return false;
        if (filter.callAllowed() != null && place.isCallAllowed() != filter.callAllowed()) return false;
        if (filter.laptopAllowed() != null && place.isLaptopAllowed() != filter.laptopAllowed()) return false;
        Set<PlaceSearchFilter.Facility> facilities = filter.facilities();
        if (facilities.contains(PlaceSearchFilter.Facility.CALL_ROOM) && !place.isCallAllowed()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.LAPTOP) && !place.isLaptopAllowed()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.OUTLETS) && !place.hasOutlets()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.WIFI) && !place.hasWifi()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.OUTSIDE_FOOD) && !place.isOutsideFoodAllowed()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.STUDY_ROOM) && !place.hasStudyRoom()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.PRINTER) && !place.hasPrinter()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.LOCKER) && !place.hasLocker()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.PARKING) && !place.hasParking()) return false;
        if (facilities.contains(PlaceSearchFilter.Facility.BEVERAGES) && !place.providesBeverages()) return false;
        if (filter.distance() != null && distanceMeters(place.getLatitude(), place.getLongitude(),
                filter.latitude(), filter.longitude()) > filter.distance()) return false;
        return true;
    }

    private PlaceSearchResponse.Place toResponse(Place place, PlaceSearchFilter filter) {
        Integer distance = filter.latitude() == null ? null
                : (int) Math.round(distanceMeters(place.getLatitude(), place.getLongitude(),
                filter.latitude(), filter.longitude()));
        return new PlaceSearchResponse.Place(place.getId().toString(), place.getName(), place.getAddress(),
                place.getLatitude(), place.getLongitude(), place.getHourlyPrice(), place.getNoiseLevel().name(),
                place.getAgeRestriction().name(), place.isCallAllowed(), place.isLaptopAllowed(), distance);
    }

    private static double distanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double latDelta = Math.toRadians(latitude2 - latitude1);
        double lonDelta = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    static String normalize(String rawQuery) {
        if (rawQuery == null) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");
        }
        String query = WHITESPACE.matcher(SPECIAL_CHARACTERS.matcher(rawQuery.trim()).replaceAll(""))
                .replaceAll(" ");
        if (query.isBlank()) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "검색어에 문자 또는 숫자를 입력해주세요.");
        }
        return query;
    }
}
