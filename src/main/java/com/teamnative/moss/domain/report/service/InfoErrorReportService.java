package com.teamnative.moss.domain.report.service;

import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.report.dto.InfoErrorReportRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.InfoIssueType;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
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
public class InfoErrorReportService {
    private static final List<ReportStatus> PENDING_STATUSES = List.of(
            ReportStatus.RECEIVED, ReportStatus.UNDER_REVIEW);

    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final UserReportRepository reportRepository;

    public InfoErrorReportService(UserRepository userRepository, PlaceRepository placeRepository,
                                  UserReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public UserReportResponse submit(String email, InfoErrorReportRequest request) {
        UserAccount reporter = findStudent(email);
        Place place = placeRepository.findByIdForReport(request.placeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."));
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다.");
        }

        String content = request.content().trim();
        if (content.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오류 내용은 공백을 제외하고 10자 이상 입력해 주세요.");
        }
        if (reportRepository.existsByPlace_IdAndReportTypeAndIssueTypeAndStatusIn(
                place.getId(), ReportType.INFO_ERROR, request.issueType(), PENDING_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 장소와 오류 항목의 제보가 이미 처리 중입니다.");
        }

        UserReport report = new UserReport(reporter, place, request.issueType(), content,
                request.photoUrls(), LocalDateTime.now());
        reportRepository.saveAndFlush(report);
        report.assignReportNumber(createReportNumber(report.getId()));
        reportRepository.flush();
        return UserReportResponse.from(report);
    }

    private UserAccount findStudent(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 계정만 정보 오류를 제보할 수 있습니다.");
        }
        return user;
    }

    private String createReportNumber(Long id) {
        return "I-%02d%05d".formatted(LocalDate.now().getYear() % 100, id);
    }
}
