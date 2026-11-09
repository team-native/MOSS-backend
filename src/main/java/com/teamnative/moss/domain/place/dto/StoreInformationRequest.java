package com.teamnative.moss.domain.place.dto;

import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.PlaceType;
import com.teamnative.moss.domain.search.entity.PreferredAgeGroup;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record StoreInformationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 500) String address,
        @NotNull PlaceType placeType,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @Size(max = 30) String contactNumber,
        @Size(max = 3000) String description,
        @Size(max = 1000) String operatingHours,
        @Size(max = 500) String holidays,
        @NotNull @Min(0) Integer minimumHourlyPrice,
        @NotNull @Min(0) Integer maximumHourlyPrice,
        @NotNull PlaceSearchFilter.AgeRestriction ageRestriction,
        @NotNull Set<PreferredAgeGroup> preferredAgeGroups,
        @NotNull PlaceSearchFilter.NoiseLevel noiseLevel,
        @NotNull CallAvailability callAvailability,
        @NotNull Set<PlaceSearchFilter.Facility> facilities
) {
    @AssertTrue(message = "최고 시간당 가격은 최저 가격보다 작을 수 없습니다.")
    public boolean hasValidPriceRange() {
        return minimumHourlyPrice != null && maximumHourlyPrice != null
                && minimumHourlyPrice <= maximumHourlyPrice;
    }

    public StoreInformationRequest {
        if (preferredAgeGroups != null) preferredAgeGroups = Set.copyOf(preferredAgeGroups);
        if (facilities != null) facilities = Set.copyOf(facilities);
    }
}
