package com.teamnative.moss.domain.auth.controller;

import com.teamnative.moss.domain.auth.dto.BusinessVerificationResponse;
import com.teamnative.moss.domain.auth.service.BusinessVerificationService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/business/verification")
@Tag(name = "Business Verification")
public class BusinessVerificationController {
    private final BusinessVerificationService verificationService;

    public BusinessVerificationController(BusinessVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사업자 인증 신청", description = "사업자 정보와 등록증 파일을 제출합니다. 보완 요청 상태에서는 재제출할 수 있습니다.")
    public ResponseEntity<ApiResponse<BusinessVerificationResponse>> submit(
            @AuthenticationPrincipal String email,
            @RequestParam String registrationNumber,
            @RequestParam String representativeName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openingDate,
            @RequestParam(defaultValue = "false") boolean privacyConsent,
            @RequestParam MultipartFile certificate) {
        BusinessVerificationResponse response = verificationService.submit(email, registrationNumber,
                representativeName, openingDate, privacyConsent, certificate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "사업자 인증 신청이 접수되었습니다.", response));
    }

    @GetMapping("/me")
    @Operation(summary = "사업자 인증 상태 조회", description = "로그인한 업주의 인증 신청 상태와 처리 사유를 조회합니다.")
    public ApiResponse<BusinessVerificationResponse> myStatus(@AuthenticationPrincipal String email) {
        return ApiResponse.success("사업자 인증 상태를 조회했습니다.", verificationService.getMyStatus(email));
    }
}
