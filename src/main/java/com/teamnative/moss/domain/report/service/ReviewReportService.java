package com.teamnative.moss.domain.report.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.report.dto.ReviewReportRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.entity.ReviewReportReason;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewReportService {
    private final UserRepository userRepository;
    private final PlaceReviewRepository reviewRepository;
    private final UserReportRepository reportRepository;

    public ReviewReportService(UserRepository userRepository, PlaceReviewRepository reviewRepository,
                               UserReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public UserReportResponse submit(String email, Long reviewId, ReviewReportRequest request) {
        UserAccount reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        PlaceReview review = reviewRepository.findByIdForReport(reviewId)
                .filter(existing -> !existing.isDeleted())
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

    private String createReportNumber(Long id) {
        return "R-%02d%06d".formatted(LocalDate.now().getYear() % 100, id);
    }
}
