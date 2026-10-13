package com.teamnative.moss.domain.auth.dto;

import com.teamnative.moss.domain.auth.entity.Grade;
import com.teamnative.moss.domain.auth.entity.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentSignupRequest(
        @NotNull(message = "가입 유형을 선택해 주세요.")
        Role role,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "이름을 입력해주세요.")
        String name,

        @NotBlank(message = "휴대폰 번호를 입력해주세요.")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
        String number,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 6, max = 15, message = "비밀번호는 6자 이상 15자 이하여야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm,

        @NotNull(message = "학년 구분을 선택해주세요.")
        Grade grade
) {
    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirm);
    }
}
