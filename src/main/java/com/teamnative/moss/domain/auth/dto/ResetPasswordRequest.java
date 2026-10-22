package com.teamnative.moss.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "재설정 토큰을 입력해주세요.")
        String resetToken,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 6, max = 15, message = "비밀번호는 6자 이상 15자 이하여야 합니다.")
        String password,
        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm
) {
    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirm);
    }
}
