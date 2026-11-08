package com.teamnative.moss.domain.search.entity;

import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "places", indexes = {
        @Index(name = "idx_places_name", columnList = "name"),
        @Index(name = "idx_places_type_location", columnList = "place_type,latitude,longitude")
})
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 30)
    private PlaceType placeType;

    @NotNull
    @Min(-90)
    @Max(90)
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Min(-180)
    @Max(180)
    @Column(nullable = false)
    private Double longitude;

    @NotNull
    @Min(0)
    @Column(name = "hourly_price", nullable = false)
    private Integer hourlyPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "noise_level", nullable = false, length = 20)
    private PlaceSearchFilter.NoiseLevel noiseLevel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "age_restriction", nullable = false, length = 30)
    private PlaceSearchFilter.AgeRestriction ageRestriction;

    @Column(name = "call_allowed", nullable = false)
    private boolean callAllowed;

    @Column(name = "laptop_allowed", nullable = false)
    private boolean laptopAllowed;

    @Column(nullable = false)
    private boolean outlets;

    @Column(nullable = false)
    private boolean wifi;

    @Column(name = "outside_food_allowed", nullable = false)
    private boolean outsideFoodAllowed;

    @Column(name = "study_room", nullable = false)
    private boolean studyRoom;

    protected Place() {
    }

    public Place(String name, String address, PlaceType placeType, double latitude, double longitude, int hourlyPrice,
                 PlaceSearchFilter.NoiseLevel noiseLevel, PlaceSearchFilter.AgeRestriction ageRestriction,
                 boolean callAllowed, boolean laptopAllowed, boolean outlets, boolean wifi,
                 boolean outsideFoodAllowed, boolean studyRoom) {
        this.name = name;
        this.address = address;
        this.placeType = placeType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hourlyPrice = hourlyPrice;
        this.noiseLevel = noiseLevel;
        this.ageRestriction = ageRestriction;
        this.callAllowed = callAllowed;
        this.laptopAllowed = laptopAllowed;
        this.outlets = outlets;
        this.wifi = wifi;
        this.outsideFoodAllowed = outsideFoodAllowed;
        this.studyRoom = studyRoom;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public PlaceType getPlaceType() { return placeType; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getHourlyPrice() { return hourlyPrice; }
    public PlaceSearchFilter.NoiseLevel getNoiseLevel() { return noiseLevel; }
    public PlaceSearchFilter.AgeRestriction getAgeRestriction() { return ageRestriction; }
    public boolean isCallAllowed() { return callAllowed; }
    public boolean isLaptopAllowed() { return laptopAllowed; }
    public boolean hasOutlets() { return outlets; }
    public boolean hasWifi() { return wifi; }
    public boolean isOutsideFoodAllowed() { return outsideFoodAllowed; }
    public boolean hasStudyRoom() { return studyRoom; }
}
