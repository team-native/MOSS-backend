package com.teamnative.moss.domain.report.service;

import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.report.dto.ReportProcessingRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudySpotReportProcessingService {
    private final UserRepository userRepository;
    private final UserReportRepository reportRepository;

    public StudySpotReportProcessingService(UserRepository userRepository, UserReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<UserReportResponse> getQueue(String email, ReportStatus status) {
        requireAdmin(email);
        List<UserReport> reports = status == null
                ? reportRepository.findByReportTypeOrderByCreatedAtAsc(ReportType.STUDY_SPOT)
                : reportRepository.findByReportTypeAndStatusOrderByCreatedAtAsc(ReportType.STUDY_SPOT, status);
        return reports.stream().map(UserReportResponse::from).toList();
    }

    @Transactional
    public UserReportResponse process(String email, Long reportId, ReportProcessingRequest request) {
        requireAdmin(email);
        UserReport report = reportRepository.findByIdAndTypeForUpdate(reportId, ReportType.STUDY_SPOT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디 스팟 제보를 찾을 수 없습니다."));
        ReportStatus current = report.getStatus();
        ReportStatus next = request.status();
        String reason = request.reason() == null || request.reason().isBlank() ? null : request.reason().trim();

        if (current == ReportStatus.RECEIVED && next == ReportStatus.UNDER_REVIEW) {
            reason = null;
        } else if (current == ReportStatus.UNDER_REVIEW
                && (next == ReportStatus.ACCEPTED || next == ReportStatus.REJECTED)) {
            if (next == ReportStatus.REJECTED && reason == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "미반영 사유를 입력해 주세요.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "현재 제보 상태에서는 해당 처리로 변경할 수 없습니다.");
        }

        report.updateProcessing(next, reason, LocalDateTime.now());
        return UserReportResponse.from(report);
    }

    private void requireAdmin(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 제보를 처리할 수 있습니다.");
        }
    }
}
