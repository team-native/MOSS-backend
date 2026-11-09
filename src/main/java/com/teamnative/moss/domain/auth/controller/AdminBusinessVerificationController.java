package com.teamnative.moss.domain.auth.controller;

import com.teamnative.moss.domain.auth.dto.BusinessVerificationResponse;
import com.teamnative.moss.domain.auth.dto.BusinessVerificationReviewRequest;
import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.service.BusinessVerificationService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/business-verifications")
@Tag(name = "Admin Business Verification")
public class AdminBusinessVerificationController {
    private final BusinessVerificationService verificationService;

    public AdminBusinessVerificationController(BusinessVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    @Operation(summary = "사업자 인증 신청 목록", description = "관리자가 신청을 상태별로 조회합니다.")
    public ApiResponse<List<BusinessVerificationResponse>> applications(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) BusinessVerificationStatus status) {
        return ApiResponse.success("사업자 인증 신청 목록을 조회했습니다.",
                verificationService.getApplications(email, status));
    }

    @GetMapping("/{applicationId}/document")
    @Operation(summary = "사업자등록증 확인", description = "관리자가 제출된 사업자등록증 파일을 확인합니다.")
    public ResponseEntity<byte[]> document(@AuthenticationPrincipal String email,
                                           @PathVariable Long applicationId) {
        BusinessVerificationService.DocumentData document = verificationService.getDocument(email, applicationId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(document.filename(), StandardCharsets.UTF_8).build());
        headers.set("X-Content-Type-Options", "nosniff");
        headers.setContentLength(document.data().length);
        return ResponseEntity.ok().headers(headers).body(document.data());
    }

    @PatchMapping("/{applicationId}")
    @Operation(summary = "사업자 인증 처리", description = "접수, 서류 확인, 승인, 보완 요청 또는 반려 상태를 처리합니다.")
    public ApiResponse<BusinessVerificationResponse> review(
            @AuthenticationPrincipal String email,
            @PathVariable Long applicationId,
            @Valid @RequestBody BusinessVerificationReviewRequest request) {
        return ApiResponse.success("사업자 인증 신청을 처리했습니다.",
                verificationService.review(email, applicationId, request.status(), request.reason()));
    }
}
