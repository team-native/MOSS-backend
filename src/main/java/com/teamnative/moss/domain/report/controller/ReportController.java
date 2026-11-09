package com.teamnative.moss.domain.report.controller;

import com.teamnative.moss.domain.report.dto.StudySpotReportRequest;
import com.teamnative.moss.domain.report.dto.StudySpotReportSubmissionResponse;
import com.teamnative.moss.domain.report.dto.InfoErrorReportRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.service.StudySpotReportService;
import com.teamnative.moss.domain.report.service.InfoErrorReportService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports")
public class ReportController {
    private final StudySpotReportService reportService;
    private final InfoErrorReportService infoErrorReportService;

    public ReportController(StudySpotReportService reportService, InfoErrorReportService infoErrorReportService) {
        this.reportService = reportService;
        this.infoErrorReportService = infoErrorReportService;
    }

    @PostMapping("/study-spots")
    @Operation(summary = "스터디 스팟 제보", description = "새로운 스터디 장소를 제보합니다. 이미 등록된 장소면 기존 장소 ID를 반환합니다.")
    public ResponseEntity<ApiResponse<StudySpotReportSubmissionResponse>> submitStudySpot(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody StudySpotReportRequest request) {
        StudySpotReportSubmissionResponse result = reportService.submit(email, request);
        if (result.alreadyRegistered()) {
            return ResponseEntity.ok(ApiResponse.success("이미 등록된 장소입니다.", result));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "스터디 스팟 제보가 접수되었습니다.", result));
    }

    @PostMapping("/info-errors")
    @Operation(summary = "장소 정보 오류 제보", description = "장소의 가격, 연령 제한, 운영 시간, 소음, 시설 등의 오류를 제보합니다.")
    public ResponseEntity<ApiResponse<UserReportResponse>> submitInfoError(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody InfoErrorReportRequest request) {
        UserReportResponse report = infoErrorReportService.submit(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "정보 오류 제보가 접수되었습니다.", report));
    }

    @GetMapping("/my")
    @Operation(summary = "내 제보 목록 조회", description = "로그인한 사용자의 제보 처리 현황을 최신순으로 조회합니다.")
    public ApiResponse<List<UserReportResponse>> myReports(@AuthenticationPrincipal String email,
                                                           @RequestParam(required = false) ReportType type) {
        return ApiResponse.success("내 제보 목록을 조회했습니다.", reportService.getMyReports(email, type));
    }

    @GetMapping("/my/{reportId}")
    @Operation(summary = "내 제보 상세 조회", description = "로그인한 사용자의 제보 내용과 처리 결과를 조회합니다.")
    public ApiResponse<UserReportResponse> myReport(@AuthenticationPrincipal String email,
                                                    @PathVariable Long reportId) {
        return ApiResponse.success("내 제보 상세를 조회했습니다.", reportService.getMyReport(email, reportId));
    }
}
