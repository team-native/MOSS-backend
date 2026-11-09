package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.entity.PreferredAgeGroup;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public record StoreInformationResponse(
        String placeId,
        String name,
        String address,
        String placeType,
        double latitude,
        double longitude,
        String contactNumber,
        String description,
        String operatingHours,
        String holidays,
        int minimumHourlyPrice,
        int maximumHourlyPrice,
        String ageRestriction,
        Set<PreferredAgeGroup> preferredAgeGroups,
        String noiseLevel,
        CallAvailability callAvailability,
        boolean laptopAllowed,
        Set<PlaceSearchFilter.Facility> facilities,
        LocalDateTime informationUpdatedAt
) {
    public StoreInformationResponse {
        preferredAgeGroups = Set.copyOf(preferredAgeGroups);
        facilities = Set.copyOf(facilities);
    }

    public static StoreInformationResponse from(Place place) {
        Set<PlaceSearchFilter.Facility> facilities = new HashSet<>();
        if (place.hasOutlets()) facilities.add(PlaceSearchFilter.Facility.OUTLETS);
        if (place.hasWifi()) facilities.add(PlaceSearchFilter.Facility.WIFI);
        if (place.isOutsideFoodAllowed()) facilities.add(PlaceSearchFilter.Facility.OUTSIDE_FOOD);
        if (place.hasStudyRoom()) facilities.add(PlaceSearchFilter.Facility.STUDY_ROOM);
        if (place.hasPrinter()) facilities.add(PlaceSearchFilter.Facility.PRINTER);
        if (place.hasLocker()) facilities.add(PlaceSearchFilter.Facility.LOCKER);
        if (place.hasParking()) facilities.add(PlaceSearchFilter.Facility.PARKING);
        if (place.providesBeverages()) facilities.add(PlaceSearchFilter.Facility.BEVERAGES);
        if (place.isLaptopAllowed()) facilities.add(PlaceSearchFilter.Facility.LAPTOP);
        if (place.isCallAllowed()) facilities.add(PlaceSearchFilter.Facility.CALL_ROOM);
        CallAvailability callAvailability = place.isCallRoomOnly() ? CallAvailability.ROOM_ONLY
                : place.isCallAllowed() ? CallAvailability.ALLOWED : CallAvailability.NOT_ALLOWED;
        return new StoreInformationResponse(place.getId().toString(), place.getName(), place.getAddress(),
                place.getPlaceType().name(), place.getLatitude(), place.getLongitude(), place.getContactNumber(),
                place.getDescription(), place.getOperatingHours(), place.getHolidays(), place.getHourlyPrice(),
                place.getMaxHourlyPrice(), place.getAgeRestriction().name(), place.getPreferredAgeGroups(),
                place.getNoiseLevel().name(), callAvailability, place.isLaptopAllowed(), facilities,
                place.getInformationUpdatedAt());
    }
}
