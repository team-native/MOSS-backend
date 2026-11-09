package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.place.dto.BusinessDashboardResponse;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessDashboardService {
    private static final List<ReportStatus> PENDING_REPORT_STATUSES =
            List.of(ReportStatus.RECEIVED, ReportStatus.UNDER_REVIEW);

    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;
    private final PlaceReviewRepository reviewRepository;
    private final UserReportRepository reportRepository;

    public BusinessDashboardService(UserRepository userRepository,
            BusinessVerificationRepository verificationRepository, PlaceRepository placeRepository,
            PlaceReviewRepository reviewRepository, UserReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public BusinessDashboardResponse getDashboard(String email, Long placeId) {
        requireApprovedBusiness(email);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

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
                pendingReports,
                reportRepository.findByPlace_IdAndReportTypeAndStatusInOrderByCreatedAtDesc(
                        placeId, ReportType.INFO_ERROR, PENDING_REPORT_STATUSES, PageRequest.of(0, 5)).getContent(),
                reviewRepository.findByPlace_IdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        placeId, PageRequest.of(0, 5)).getContent());
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
