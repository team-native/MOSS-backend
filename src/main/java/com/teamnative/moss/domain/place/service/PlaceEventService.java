package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.place.dto.PlaceEventRequest;
import com.teamnative.moss.domain.place.dto.PlaceEventResponse;
import com.teamnative.moss.domain.place.entity.PlaceEvent;
import com.teamnative.moss.domain.place.repository.PlaceEventRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaceEventService {
    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;
    private final PlaceEventRepository eventRepository;

    public PlaceEventService(UserRepository userRepository,
            BusinessVerificationRepository verificationRepository, PlaceRepository placeRepository,
            PlaceEventRepository eventRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getBusinessEvents(String email, Long placeId) {
        requireApprovedBusiness(email);
        requirePlace(placeId);
        return eventRepository.findByPlace_IdOrderByStartDateAscIdAsc(placeId)
                .stream().map(PlaceEventResponse::from).toList();
    }

    @Transactional
    public PlaceEventResponse create(String email, Long placeId, PlaceEventRequest request) {
        requireApprovedBusiness(email);
        Place place = requirePlace(placeId);
        validateDates(request);
        PlaceEvent event = new PlaceEvent(place, request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), LocalDateTime.now());
        return PlaceEventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public PlaceEventResponse update(String email, Long placeId, Long eventId, PlaceEventRequest request) {
        requireApprovedBusiness(email);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        validateDates(request);
        event.update(request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), LocalDateTime.now());
        return PlaceEventResponse.from(event);
    }

    @Transactional
    public void delete(String email, Long placeId, Long eventId) {
        requireApprovedBusiness(email);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getPublicActiveEvents(Long placeId) {
        Place place = requirePlace(placeId);
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다.");
        }
        LocalDate today = LocalDate.now();
        return eventRepository.findByPlace_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscIdAsc(
                        placeId, today, today)
                .stream().map(PlaceEventResponse::from).toList();
    }

    private void validateDates(PlaceEventRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 시작일은 종료일보다 늦을 수 없습니다.");
        }
        String title = request.title().trim();
        if (title.length() < 2 || title.length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 제목은 2자 이상 40자 이하여야 합니다.");
        }
    }

    private Place requirePlace(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
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
}
