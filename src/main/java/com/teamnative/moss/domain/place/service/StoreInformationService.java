package com.teamnative.moss.domain.place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.place.dto.CallAvailability;
import com.teamnative.moss.domain.place.dto.StoreInformationRequest;
import com.teamnative.moss.domain.place.dto.StoreInformationResponse;
import com.teamnative.moss.domain.place.entity.PlaceInformationRevision;
import com.teamnative.moss.domain.place.repository.PlaceInformationRevisionRepository;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StoreInformationService {
    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;
    private final PlaceInformationRevisionRepository revisionRepository;
    private final ObjectMapper objectMapper;

    public StoreInformationService(UserRepository userRepository,
                                   BusinessVerificationRepository verificationRepository,
                                   PlaceRepository placeRepository,
                                   PlaceInformationRevisionRepository revisionRepository,
                                   ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
        this.revisionRepository = revisionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public StoreInformationResponse getInformation(String email, Long placeId) {
        requireApprovedBusiness(email);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        return StoreInformationResponse.from(place);
    }

    @Transactional
    public StoreInformationResponse updateInformation(String email, Long placeId,
                                                      StoreInformationRequest request) {
        UserAccount business = requireApprovedBusiness(email);
        Place place = placeRepository.findByIdForInformationUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        StoreInformationResponse previous = StoreInformationResponse.from(place);
        place.updateInformation(request.name().trim(), request.address().trim(), request.placeType(),
                request.latitude(), request.longitude(), normalizeOptional(request.contactNumber()),
                normalizeOptional(request.description()), normalizeOptional(request.operatingHours()),
                normalizeOptional(request.holidays()), request.minimumHourlyPrice(), request.maximumHourlyPrice(),
                request.ageRestriction(), request.preferredAgeGroups(), request.noiseLevel(),
                request.facilities().contains(PlaceSearchFilter.Facility.OUTLETS),
                request.facilities().contains(PlaceSearchFilter.Facility.WIFI),
                request.facilities().contains(PlaceSearchFilter.Facility.PRINTER),
                request.facilities().contains(PlaceSearchFilter.Facility.LOCKER),
                request.facilities().contains(PlaceSearchFilter.Facility.PARKING),
                request.facilities().contains(PlaceSearchFilter.Facility.BEVERAGES),
                request.facilities().contains(PlaceSearchFilter.Facility.STUDY_ROOM),
                request.facilities().contains(PlaceSearchFilter.Facility.OUTSIDE_FOOD),
                request.callAvailability() != CallAvailability.NOT_ALLOWED,
                request.callAvailability() == CallAvailability.ROOM_ONLY,
                request.facilities().contains(PlaceSearchFilter.Facility.LAPTOP));

        StoreInformationResponse updated = StoreInformationResponse.from(place);
        revisionRepository.save(new PlaceInformationRevision(place, business, toJson(previous), toJson(updated),
                LocalDateTime.now()));
        return updated;
    }

    private UserAccount requireApprovedBusiness(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "업주 계정만 이용할 수 있습니다.");
        }
        boolean approved = verificationRepository.findByUser_Id(user.getId())
                .map(verification -> verification.getStatus() == BusinessVerificationStatus.APPROVED)
                .orElse(false);
        if (!approved) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사업자 인증 승인 후 이용할 수 있습니다.");
        }
        return user;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String toJson(StoreInformationResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("매장 정보 수정 이력을 저장할 수 없습니다.", exception);
        }
    }
}
