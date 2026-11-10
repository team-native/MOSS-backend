package com.teamnative.moss.domain.report.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.report.dto.ReviewReportRequest;
import com.teamnative.moss.domain.report.dto.ReviewReportProcessingRequest;
import com.teamnative.moss.domain.report.dto.ReviewReportAdminResponse;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.entity.ReviewReportOutcome;
import com.teamnative.moss.domain.report.entity.ReviewReportReason;
import com.teamnative.moss.domain.report.entity.ReviewSanctionLevel;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import com.teamnative.moss.domain.notification.service.NotificationService;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewReportService {
    private final UserRepository userRepository;
    private final PlaceReviewRepository reviewRepository;
    private final UserReportRepository reportRepository;
    private final NotificationService notificationService;

    public ReviewReportService(UserRepository userRepository, PlaceReviewRepository reviewRepository,
                               UserReportRepository reportRepository,
                               NotificationService notificationService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public UserReportResponse submit(String email, Long reviewId, ReviewReportRequest request) {
        UserAccount reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        PlaceReview review = reviewRepository.findByIdForReport(reviewId)
                .filter(existing -> !existing.isDeleted() && !existing.isModerated())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        String detail = request.detail() == null || request.detail().isBlank() ? null : request.detail().trim();
        if (request.reason() == ReviewReportReason.OTHER && detail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "기타 사유의 상세 내용을 입력해 주세요.");
        }
        if (reportRepository.existsByReporter_IdAndReview_Id(reporter.getId(), reviewId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 리뷰입니다.");
        }

        UserReport report = new UserReport(reporter, review, request.reason(), detail, LocalDateTime.now());
        reportRepository.saveAndFlush(report);
        report.assignReportNumber(createReportNumber(report.getId()));
        reportRepository.flush();
        return UserReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReviewReportAdminResponse> getQueue(String email, ReportStatus status) {
        requireAdmin(email);
        List<UserReport> reports = status == null
                ? reportRepository.findByReportTypeAndStatusInOrderByCreatedAtAsc(
                        ReportType.REVIEW, List.of(ReportStatus.RECEIVED, ReportStatus.UNDER_REVIEW))
                : reportRepository.findByReportTypeAndStatusOrderByCreatedAtAsc(ReportType.REVIEW, status);
        return reports.stream().map(ReviewReportAdminResponse::from).toList();
    }

    @Transactional
    public ReviewReportAdminResponse process(String email, Long reportId,
                                             ReviewReportProcessingRequest request) {
        requireAdmin(email);
        UserReport report = reportRepository.findByIdAndTypeForUpdate(reportId, ReportType.REVIEW)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 신고를 찾을 수 없습니다."));
        LocalDateTime now = LocalDateTime.now();
        String reason = request.reason() == null || request.reason().isBlank() ? null : request.reason().trim();

        if (request.decision() == ReviewReportProcessingRequest.Decision.START_REVIEW) {
            if (report.getStatus() != ReportStatus.RECEIVED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "접수된 신고만 검토 중으로 변경할 수 있습니다.");
            }
            report.updateProcessing(ReportStatus.UNDER_REVIEW, null, now);
            return ReviewReportAdminResponse.from(report);
        }
        if (report.getStatus() != ReportStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "검토 중인 신고만 완료 처리할 수 있습니다.");
        }

        if (request.decision() == ReviewReportProcessingRequest.Decision.DISMISS) {
            if (reason == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "미조치 사유를 입력해 주세요.");
            }
            report.completeReviewProcessing(ReviewReportOutcome.NO_VIOLATION, null, null, reason, now);
            notificationService.create(report.getReporter(), NotificationType.REVIEW_REPORT_RESULT,
                    "리뷰 신고 검토 완료", "신고한 리뷰를 검토했으며 정책 위반 사항이 확인되지 않았습니다.",
                    "/reports/my/" + report.getId());
            return ReviewReportAdminResponse.from(report);
        }

        var review = reviewRepository.findByIdForModeration(report.getReview().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고 대상 리뷰를 찾을 수 없습니다."));
        ReviewSanctionLevel sanctionLevel = null;
        LocalDateTime sanctionUntil = null;
        if (review.moderate(now)) {
            var author = userRepository.findByIdForReviewModeration(review.getAuthor().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 작성자를 찾을 수 없습니다."));
            int violationCount = author.recordReviewViolation();
            sanctionLevel = sanctionLevel(violationCount);
            long restrictionDays = restrictionDays(sanctionLevel);
            if (restrictionDays > 0) {
                sanctionUntil = author.extendReviewRestriction(now, restrictionDays);
            }
            notifyReviewAuthor(author, sanctionLevel, sanctionUntil);
        }

        report.completeReviewProcessing(ReviewReportOutcome.VIOLATION_CONFIRMED,
                sanctionLevel, sanctionUntil, reason, now);
        notificationService.create(report.getReporter(), NotificationType.REVIEW_REPORT_RESULT,
                "리뷰 신고 검토 완료", "신고한 리뷰를 검토했으며 정책에 따라 조치했습니다.",
                "/reports/my/" + report.getId());
        return ReviewReportAdminResponse.from(report);
    }

    private void notifyReviewAuthor(com.teamnative.moss.domain.auth.entity.UserAccount author,
                                    ReviewSanctionLevel level, LocalDateTime restrictedUntil) {
        String message = switch (level) {
            case WARNING -> "작성하신 리뷰가 운영 정책 위반으로 확인되어 숨김 처리되었습니다. 같은 문제가 반복되면 리뷰 기능이 제한될 수 있습니다.";
            case RESTRICTION_3_DAYS, RESTRICTION_7_DAYS, RESTRICTION_30_DAYS ->
                    "작성하신 리뷰가 운영 정책 위반으로 확인되어 숨김 처리되었습니다. 리뷰 기능은 "
                            + restrictedUntil + "까지 제한됩니다.";
        };
        notificationService.create(author, NotificationType.REVIEW_MODERATION_NOTICE,
                "커뮤니티 운영 정책 안내", message, "/review/my");
    }

    private ReviewSanctionLevel sanctionLevel(int violationCount) {
        return switch (violationCount) {
            case 1 -> ReviewSanctionLevel.WARNING;
            case 2 -> ReviewSanctionLevel.RESTRICTION_3_DAYS;
            case 3 -> ReviewSanctionLevel.RESTRICTION_7_DAYS;
            default -> ReviewSanctionLevel.RESTRICTION_30_DAYS;
        };
    }

    private long restrictionDays(ReviewSanctionLevel level) {
        return switch (level) {
            case WARNING -> 0;
            case RESTRICTION_3_DAYS -> 3;
            case RESTRICTION_7_DAYS -> 7;
            case RESTRICTION_30_DAYS -> 30;
        };
    }

    private void requireAdmin(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 리뷰 신고를 처리할 수 있습니다.");
        }
    }

    private String createReportNumber(Long id) {
        return "R-%02d%06d".formatted(LocalDate.now().getYear() % 100, id);
    }
}
