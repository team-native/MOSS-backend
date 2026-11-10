package com.teamnative.moss.domain.search.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "places", indexes = {
        @Index(name = "idx_places_name", columnList = "name"),
        @Index(name = "idx_places_type_location", columnList = "place_type,latitude,longitude"),
        @Index(name = "idx_places_owner", columnList = "owner_id")
})
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

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

    @Column(name = "max_hourly_price")
    private Integer maxHourlyPrice;

    @Column(name = "operating_hours", columnDefinition = "text")
    private String operatingHours;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String holidays;

    @ElementCollection
    @CollectionTable(name = "place_preferred_age_groups", joinColumns = @JoinColumn(name = "place_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    private Set<PreferredAgeGroup> preferredAgeGroups = new HashSet<>();

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

    @Column(name = "call_room_only", nullable = false, columnDefinition = "boolean default false")
    private boolean callRoomOnly;

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

    @Column(nullable = false)
    private boolean printer;

    @Column(nullable = false)
    private boolean locker;

    @Column(nullable = false)
    private boolean parking;

    @Column(nullable = false)
    private boolean beverages;

    @Column(name = "publicly_visible", nullable = false, columnDefinition = "boolean default true")
    private boolean publiclyVisible = true;

    @Column(name = "information_updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime informationUpdatedAt;

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
        this.maxHourlyPrice = hourlyPrice;
        this.noiseLevel = noiseLevel;
        this.ageRestriction = ageRestriction;
        this.callAllowed = callAllowed;
        this.laptopAllowed = laptopAllowed;
        this.outlets = outlets;
        this.wifi = wifi;
        this.outsideFoodAllowed = outsideFoodAllowed;
        this.studyRoom = studyRoom;
        this.informationUpdatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void setInformationCreatedAt() {
        if (informationUpdatedAt == null) informationUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void setInformationUpdatedAt() {
        informationUpdatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    @JsonIgnore
    public UserAccount getOwner() { return owner; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public PlaceType getPlaceType() { return placeType; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getHourlyPrice() { return hourlyPrice; }
    public int getMaxHourlyPrice() { return maxHourlyPrice == null ? hourlyPrice : maxHourlyPrice; }
    public String getOperatingHours() { return operatingHours; }
    public String getContactNumber() { return contactNumber; }
    public String getDescription() { return description; }
    public String getHolidays() { return holidays; }
    public Set<PreferredAgeGroup> getPreferredAgeGroups() { return Set.copyOf(preferredAgeGroups); }
    public PlaceSearchFilter.NoiseLevel getNoiseLevel() { return noiseLevel; }
    public PlaceSearchFilter.AgeRestriction getAgeRestriction() { return ageRestriction; }
    public boolean isCallAllowed() { return callAllowed; }
    public boolean isCallRoomOnly() { return callRoomOnly; }
    public boolean isLaptopAllowed() { return laptopAllowed; }
    public boolean hasOutlets() { return outlets; }
    public boolean hasWifi() { return wifi; }
    public boolean isOutsideFoodAllowed() { return outsideFoodAllowed; }
    public boolean hasStudyRoom() { return studyRoom; }
    public boolean hasPrinter() { return printer; }
    public boolean hasLocker() { return locker; }
    public boolean hasParking() { return parking; }
    public boolean providesBeverages() { return beverages; }
    public boolean isPubliclyVisible() { return publiclyVisible; }
    public LocalDateTime getInformationUpdatedAt() { return informationUpdatedAt; }

    public void changeVisibility(boolean publiclyVisible) {
        this.publiclyVisible = publiclyVisible;
    }

    public void assignOwner(UserAccount owner) {
        if (owner == null) throw new IllegalArgumentException("매장 소유자는 필수입니다.");
        if (this.owner != null && !this.owner.getId().equals(owner.getId())) {
            throw new IllegalStateException("이미 다른 소유자가 지정된 매장입니다.");
        }
        this.owner = owner;
    }

    public void updateDetailData(Integer maxHourlyPrice, String operatingHours, boolean callRoomOnly) {
        this.maxHourlyPrice = maxHourlyPrice;
        this.operatingHours = operatingHours;
        this.callRoomOnly = callRoomOnly;
    }

    public void updateInformation(String name, String address, PlaceType placeType, double latitude,
                                  double longitude, String contactNumber, String description,
                                  String operatingHours, String holidays, int hourlyPrice, int maxHourlyPrice,
                                  PlaceSearchFilter.AgeRestriction ageRestriction,
                                  Set<PreferredAgeGroup> preferredAgeGroups, PlaceSearchFilter.NoiseLevel noiseLevel,
                                  boolean outlets, boolean wifi, boolean printer, boolean locker, boolean parking,
                                  boolean beverages, boolean studyRoom, boolean outsideFoodAllowed,
                                  boolean callAllowed, boolean callRoomOnly, boolean laptopAllowed) {
        this.name = name;
        this.address = address;
        this.placeType = placeType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
        this.description = description;
        this.operatingHours = operatingHours;
        this.holidays = holidays;
        this.hourlyPrice = hourlyPrice;
        this.maxHourlyPrice = maxHourlyPrice;
        this.ageRestriction = ageRestriction;
        this.preferredAgeGroups.clear();
        this.preferredAgeGroups.addAll(preferredAgeGroups);
        this.noiseLevel = noiseLevel;
        this.outlets = outlets;
        this.wifi = wifi;
        this.printer = printer;
        this.locker = locker;
        this.parking = parking;
        this.beverages = beverages;
        this.studyRoom = studyRoom;
        this.outsideFoodAllowed = outsideFoodAllowed;
        this.callAllowed = callAllowed;
        this.callRoomOnly = callRoomOnly;
        this.laptopAllowed = laptopAllowed;
        this.informationUpdatedAt = LocalDateTime.now();
    }
}
