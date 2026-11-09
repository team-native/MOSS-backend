package com.teamnative.moss.domain.report.service;

import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.report.dto.StudySpotReportRequest;
import com.teamnative.moss.domain.report.dto.StudySpotReportSubmissionResponse;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
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
public class StudySpotReportService {
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final UserReportRepository reportRepository;

    public StudySpotReportService(UserRepository userRepository, PlaceRepository placeRepository,
                                  UserReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public StudySpotReportSubmissionResponse submit(String email, StudySpotReportRequest request) {
        UserAccount reporter = findStudent(email);
        String placeName = request.placeName().trim();
        String address = request.address().trim();
        Place existingPlace = placeRepository.findFirstByNameIgnoreCaseAndAddressIgnoreCase(placeName, address)
                .orElse(null);
        if (existingPlace != null) {
            return new StudySpotReportSubmissionResponse(true, existingPlace.getId().toString(), null);
        }

        LocalDateTime now = LocalDateTime.now();
        UserReport report = new UserReport(reporter, ReportType.STUDY_SPOT, placeName, address,
                request.placeType(), normalize(request.recommendationReason()), request.photoUrls(), now);
        reportRepository.saveAndFlush(report);
        report.assignReportNumber(createReportNumber(report.getId()));
        reportRepository.flush();
        return new StudySpotReportSubmissionResponse(false, null, UserReportResponse.from(report));
    }

    @Transactional(readOnly = true)
    public List<UserReportResponse> getMyReports(String email, ReportType type) {
        UserAccount reporter = findStudent(email);
        List<UserReport> reports = type == null
                ? reportRepository.findByReporter_IdOrderByCreatedAtDesc(reporter.getId())
                : reportRepository.findByReporter_IdAndReportTypeOrderByCreatedAtDesc(reporter.getId(), type);
        return reports.stream().map(UserReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserReportResponse getMyReport(String email, Long reportId) {
        UserAccount reporter = findStudent(email);
        UserReport report = reportRepository.findByIdAndReporter_Id(reportId, reporter.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제보를 찾을 수 없습니다."));
        return UserReportResponse.from(report);
    }

    private UserAccount findStudent(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 계정만 제보할 수 있습니다.");
        }
        return user;
    }

    private String createReportNumber(Long id) {
        return "S-%02d%05d".formatted(LocalDate.now().getYear() % 100, id);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
