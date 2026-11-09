package com.teamnative.moss.domain.report.controller;

import com.teamnative.moss.domain.report.dto.ReportProcessingRequest;
import com.teamnative.moss.domain.report.dto.UserReportResponse;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.service.StudySpotReportProcessingService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports/study-spots")
@Tag(name = "Admin Reports")
public class AdminStudySpotReportController {
    private final StudySpotReportProcessingService processingService;

    public AdminStudySpotReportController(StudySpotReportProcessingService processingService) {
        this.processingService = processingService;
    }

    @GetMapping
    @Operation(summary = "스터디 스팟 제보 처리 목록", description = "관리자가 접수된 스터디 스팟 제보를 상태별로 조회합니다.")
    public ApiResponse<List<UserReportResponse>> getQueue(@AuthenticationPrincipal String email,
                                                           @RequestParam(required = false) ReportStatus status) {
        return ApiResponse.success("스터디 스팟 제보 목록을 조회했습니다.", processingService.getQueue(email, status));
    }

    @PatchMapping("/{reportId}")
    @Operation(summary = "스터디 스팟 제보 처리", description = "제보를 검토 중, 반영 완료 또는 미반영 상태로 처리합니다.")
    public ApiResponse<UserReportResponse> process(@AuthenticationPrincipal String email,
                                                   @PathVariable Long reportId,
                                                   @Valid @RequestBody ReportProcessingRequest request) {
        return ApiResponse.success("스터디 스팟 제보를 처리했습니다.",
                processingService.process(email, reportId, request));
    }
}
