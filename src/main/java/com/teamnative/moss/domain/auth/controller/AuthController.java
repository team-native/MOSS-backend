package com.teamnative.moss.domain.auth.controller;

import com.teamnative.moss.domain.auth.dto.BusinessSignupRequest;
import com.teamnative.moss.domain.auth.dto.FindIdRequest;
import com.teamnative.moss.domain.auth.dto.FindIdResponse;
import com.teamnative.moss.domain.auth.dto.LoginRequest;
import com.teamnative.moss.domain.auth.dto.LoginResponse;
import com.teamnative.moss.domain.auth.dto.RefreshTokenRequest;
import com.teamnative.moss.domain.auth.dto.ResetPasswordRequest;
import com.teamnative.moss.domain.auth.dto.SignupResponse;
import com.teamnative.moss.domain.auth.dto.SendEmailRequest;
import com.teamnative.moss.domain.auth.dto.VerifyEmailRequest;
import com.teamnative.moss.domain.auth.dto.VerifyEmailResponse;
import com.teamnative.moss.domain.auth.dto.StudentSignupRequest;
import com.teamnative.moss.domain.auth.service.BusinessSignupService;
import com.teamnative.moss.domain.auth.service.FindIdService;
import com.teamnative.moss.domain.auth.service.EmailVerificationService;
import com.teamnative.moss.domain.auth.service.LoginService;
import com.teamnative.moss.domain.auth.service.StudentSignupService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
    private final StudentSignupService studentSignupService;
    private final BusinessSignupService businessSignupService;
    private final LoginService loginService;
    private final FindIdService findIdService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            StudentSignupService studentSignupService,
            BusinessSignupService businessSignupService,
            LoginService loginService,
            FindIdService findIdService,
            EmailVerificationService emailVerificationService
    ) {
        this.studentSignupService = studentSignupService;
        this.businessSignupService = businessSignupService;
        this.loginService = loginService;
        this.findIdService = findIdService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/signup/business")
    @Operation(summary = "업주 회원가입", description = "업주 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<SignupResponse>> signupBusiness(@Valid @RequestBody BusinessSignupRequest request) {
        SignupResponse response = businessSignupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/signup/student")
    @Operation(summary = "학생 회원가입", description = "학생 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<SignupResponse>> signupStudent(@Valid @RequestBody StudentSignupRequest request) {
        SignupResponse response = studentSignupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 인증 토큰을 발급합니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("로그인에 성공했습니다.", loginService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "유효한 refresh token으로 인증 토큰을 갱신합니다.")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("토큰이 갱신되었습니다.", loginService.refresh(request));
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "이름과 전화번호로 가입된 이메일과 계정 생성일을 조회합니다.")
    public ApiResponse<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        return ApiResponse.success("계정 정보를 찾았습니다.", findIdService.findId(request));
    }

    @PostMapping("/send-email")
    @Operation(summary = "이메일 인증번호 발송", description = "입력한 이메일 주소로 2분간 유효한 인증번호를 발송합니다.")
    public ApiResponse<Void> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailVerificationService.sendVerificationCode(request);
        return ApiResponse.success("인증번호를 이메일로 발송했습니다.", null);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증번호 확인", description = "유효한 인증번호를 확인하고 비밀번호 재설정 토큰을 발급합니다.")
    public ApiResponse<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ApiResponse.success("이메일 인증에 성공했습니다.", emailVerificationService.verifyEmail(request));
    }

    @PatchMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "이메일 인증으로 발급된 토큰을 사용해 비밀번호를 재설정합니다.")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        emailVerificationService.resetPassword(request);
        return ApiResponse.success("비밀번호가 변경되었습니다.", null);
    }
}
