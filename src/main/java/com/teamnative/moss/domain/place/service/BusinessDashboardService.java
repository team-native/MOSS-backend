package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.place.dto.BusinessDashboardResponse;
import com.teamnative.moss.domain.place.entity.PlaceEventStatus;
import com.teamnative.moss.domain.place.repository.PlaceEventRepository;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.entity.Place;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessDashboardService {
    private static final List<ReportStatus> PENDING_REPORT_STATUSES =
            List.of(ReportStatus.RECEIVED, ReportStatus.UNDER_REVIEW);

    private final BusinessPlaceAccessService businessPlaceAccessService;
    private final PlaceReviewRepository reviewRepository;
    private final UserReportRepository reportRepository;
    private final PlaceEventRepository eventRepository;

    public BusinessDashboardService(BusinessPlaceAccessService businessPlaceAccessService,
            PlaceReviewRepository reviewRepository, UserReportRepository reportRepository,
            PlaceEventRepository eventRepository) {
        this.businessPlaceAccessService = businessPlaceAccessService;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public BusinessDashboardResponse getDashboard(String email, Long placeId) {
        Place place = businessPlaceAccessService.requireOwnedPlace(email, placeId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);
        long recentCount = reviewRepository.countByPlace_IdAndDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                placeId, sevenDaysAgo, now);
        long previousCount = reviewRepository.countByPlace_IdAndDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                placeId, fourteenDaysAgo, sevenDaysAgo);
        long pendingReports = reportRepository.countByPlace_IdAndReportTypeAndStatusIn(
                placeId, ReportType.INFO_ERROR, PENDING_REPORT_STATUSES);

        return BusinessDashboardResponse.from(place,
                reviewRepository.countByPlace_IdAndDeletedAtIsNull(placeId), recentCount, previousCount,
                reviewRepository.countByPlace_IdAndDeletedAtIsNullAndBusinessReplyIsNull(placeId),
                eventRepository.countByPlace_IdAndApprovalStatusAndDeletedAtIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        placeId, PlaceEventStatus.APPROVED, LocalDate.now(), LocalDate.now()),
                pendingReports,
                reportRepository.findByPlace_IdAndReportTypeAndStatusInOrderByCreatedAtDesc(
                        placeId, ReportType.INFO_ERROR, PENDING_REPORT_STATUSES, PageRequest.of(0, 5)).getContent(),
                reviewRepository.findByPlace_IdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        placeId, PageRequest.of(0, 5)).getContent());
    }

}
