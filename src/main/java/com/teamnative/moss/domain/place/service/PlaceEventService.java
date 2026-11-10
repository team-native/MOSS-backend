package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.place.dto.PlaceEventRequest;
import com.teamnative.moss.domain.place.dto.PlaceEventResponse;
import com.teamnative.moss.domain.place.dto.PlaceEventReviewRequest;
import com.teamnative.moss.domain.place.dto.PlaceEventReviewResponse;
import com.teamnative.moss.domain.place.entity.PlaceEvent;
import com.teamnative.moss.domain.place.entity.PlaceEventStatus;
import com.teamnative.moss.domain.place.repository.PlaceEventRepository;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import com.teamnative.moss.domain.notification.service.NotificationService;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PlaceEventService(BusinessPlaceAccessService businessPlaceAccessService,
                             PlaceRepository placeRepository,
                             PlaceEventRepository eventRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.businessPlaceAccessService = businessPlaceAccessService;
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getBusinessEvents(String email, Long placeId) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        return eventRepository.findByPlace_IdAndDeletedAtIsNullOrderByStartDateAscIdAsc(placeId)
                .stream().map(PlaceEventResponse::from).toList();
    }

    @Transactional
    public PlaceEventResponse create(String email, Long placeId, PlaceEventRequest request) {
        Place place = placeRepository.findByIdForEventUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        businessPlaceAccessService.requireOwnedPlace(email, place);
        validateDates(request);
        validateRegistrationLimits(placeId, request, null);
        LocalDateTime now = LocalDateTime.now();
        PlaceEvent event = new PlaceEvent(place, request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), now);
        return PlaceEventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public PlaceEventResponse update(String email, Long placeId, Long eventId, PlaceEventRequest request) {
        Place place = placeRepository.findByIdForEventUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        businessPlaceAccessService.requireOwnedPlace(email, place);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        if (event.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다.");
        }
        if (event.getDisplayStatus(LocalDate.now()) == PlaceEventStatus.ENDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "종료된 이벤트는 수정할 수 없습니다.");
        }
        validateDates(request);
        validateRegistrationLimits(placeId, request, eventId);
        event.update(request.title().trim(), request.startDate(), request.endDate(),
                request.targetTimeSlots(), request.content().trim(), LocalDateTime.now());
        return PlaceEventResponse.from(event);
    }

    @Transactional
    public void delete(String email, Long placeId, Long eventId) {
        Place place = placeRepository.findByIdForEventUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        businessPlaceAccessService.requireOwnedPlace(email, place);
        PlaceEvent event = eventRepository.findByIdAndPlace_Id(eventId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        if (event.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다.");
        }
        event.softDelete(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<PlaceEventResponse> getPublicActiveEvents(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다.");
        }
        LocalDate today = LocalDate.now();
        return eventRepository.findByPlace_IdAndApprovalStatusAndDeletedAtIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscIdAsc(
                        placeId, PlaceEventStatus.APPROVED, today, today)
                .stream().map(PlaceEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceEventReviewResponse> getPendingReviews(String email) {
        requireAdmin(email);
        return eventRepository.findPendingReviews(PlaceEventStatus.PENDING_APPROVAL).stream()
                .map(PlaceEventReviewResponse::from).toList();
    }

    @Transactional
    public PlaceEventReviewResponse review(String email, Long eventId, PlaceEventReviewRequest request) {
        requireAdmin(email);
        PlaceEvent event = eventRepository.findByIdForUpdate(eventId)
                .filter(candidate -> candidate.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        if (event.getApprovalStatus() != PlaceEventStatus.PENDING_APPROVAL && !event.hasPendingRevision()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "검토 대기 중인 이벤트가 아닙니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.decision() == PlaceEventReviewRequest.Decision.APPROVE) {
            event.approve(now);
            notificationService.create(event.getPlace().getOwner(), NotificationType.PLACE_EVENT_APPROVED,
                    "이벤트 승인", "등록하신 이벤트가 승인되었습니다.",
                    "/business/places/" + event.getPlace().getId() + "/events");
        } else {
            String reason = request.reason() == null || request.reason().isBlank()
                    ? null : request.reason().trim();
            if (reason == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "반려 사유를 입력해 주세요.");
            }
            event.reject(reason, now);
            notificationService.create(event.getPlace().getOwner(), NotificationType.PLACE_EVENT_REJECTED,
                    "이벤트 반려", "등록하신 이벤트가 반려되었습니다: " + reason,
                    "/business/places/" + event.getPlace().getId() + "/events");
        }
        return PlaceEventReviewResponse.from(event);
    }

    private void validateRegistrationLimits(Long placeId, PlaceEventRequest request, Long excludeId) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long registrationsThisWeek = eventRepository.countByPlace_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                placeId, weekStart.atStartOfDay(), weekStart.plusWeeks(1).atStartOfDay());
        if (excludeId == null && registrationsThisWeek >= 2) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이벤트는 주 2회까지 등록할 수 있습니다.");
        }
        long overlapping = eventRepository.countOverlappingEvents(placeId, request.startDate(), request.endDate(),
                PlaceEventStatus.REJECTED, excludeId);
        if (overlapping >= 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "동시에 진행할 수 있는 이벤트는 최대 3개입니다.");
        }
    }

    private UserAccount requireAdmin(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 이벤트를 검토할 수 있습니다.");
        }
        return user;
    }

    private void validateDates(PlaceEventRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 시작일은 종료일보다 늦을 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(request.startDate(), request.endDate()) >= 14) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 기간은 최대 14일입니다.");
        }
        String title = request.title().trim();
        if (title.length() < 2 || title.length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 제목은 2자 이상 40자 이하여야 합니다.");
        }
        if (request.content().trim().isEmpty() || request.content().trim().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 내용은 1자 이상 500자 이하여야 합니다.");
        }
    }
}
