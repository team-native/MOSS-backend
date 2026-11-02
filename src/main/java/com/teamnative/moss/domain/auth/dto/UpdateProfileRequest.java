package com.teamnative.moss.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하여야 합니다.")
        String name,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "전화번호 형식이 올바르지 않습니다.")
        String number,

        @Size(min = 6, max = 15, message = "비밀번호는 6자 이상 15자 이하여야 합니다.")
        String password
) {
    @AssertTrue(message = "변경할 회원 정보를 하나 이상 입력해주세요.")
    public boolean hasChanges() {
        return name != null || email != null || number != null || password != null;
    }

    @AssertTrue(message = "변경할 값은 비워둘 수 없습니다.")
    public boolean hasNoBlankValues() {
        return (name == null || !name.isBlank())
                && (email == null || !email.isBlank())
                && (number == null || !number.isBlank())
                && (password == null || !password.isBlank());
    }
}
