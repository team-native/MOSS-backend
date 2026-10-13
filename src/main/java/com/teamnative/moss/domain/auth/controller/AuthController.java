package com.teamnative.moss.domain.auth.controller;

import com.teamnative.moss.domain.auth.dto.LoginRequest;
import com.teamnative.moss.domain.auth.dto.LoginResponse;
import com.teamnative.moss.domain.auth.dto.SignupResponse;
import com.teamnative.moss.domain.auth.dto.StudentSignupRequest;
import com.teamnative.moss.domain.auth.dto.BusinessSignupRequest;
import com.teamnative.moss.domain.auth.service.BusinessSignupService;
import com.teamnative.moss.domain.auth.service.StudentSignupService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final StudentSignupService studentSignupService;
    private final BusinessSignupService businessSignupService;

    public AuthController(StudentSignupService studentSignupService, BusinessSignupService businessSignupService) {
        this.studentSignupService = studentSignupService;
        this.businessSignupService = businessSignupService;
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
    @Operation(summary = "로그인", description = "아이디/비밀번호로 로그인하여 액세스 토큰을 발급받습니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = new LoginResponse(
                "replace-with-jwt-token",
                "Bearer",
                request.username()
        );
        return ApiResponse.success("로그인에 성공했습니다.", response);
    }
}
