package com.teamnative.moss.domain.place.service;

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
    private final BusinessPlaceAccessService businessPlaceAccessService;
    private final PlaceRepository placeRepository;
    private final PlaceEventRepository eventRepository;

    public PlaceEventService(BusinessPlaceAccessService businessPlaceAccessService,
                             PlaceRepository placeRepository,
                             PlaceEventRepository eventRepository) {
        this.businessPlaceAccessService = businessPlaceAccessService;
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getBusinessEvents(String email, Long placeId) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        return eventRepository.findByPlace_IdOrderByStartDateAscIdAsc(placeId)
                .stream().map(PlaceEventResponse::from).toList();
    }

    @Transactional
    public PlaceEventResponse create(String email, Long placeId, PlaceEventRequest request) {
        var place = businessPlaceAccessService.requireOwnedPlace(email, placeId);
        validateDates(request);
        PlaceEvent event = new PlaceEvent(place, request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), LocalDateTime.now());
        return PlaceEventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public PlaceEventResponse update(String email, Long placeId, Long eventId, PlaceEventRequest request) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        validateDates(request);
        event.update(request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), LocalDateTime.now());
        return PlaceEventResponse.from(event);
    }

    @Transactional
    public void delete(String email, Long placeId, Long eventId) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getPublicActiveEvents(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
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
}
