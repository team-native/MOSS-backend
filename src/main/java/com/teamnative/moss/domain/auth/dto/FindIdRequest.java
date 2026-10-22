package com.teamnative.moss.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FindIdRequest(
        @NotBlank(message = "이름을 입력해 주세요.")
        String name,

        @NotBlank(message = "전화번호를 입력해 주세요.")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "전화번호 형식이 올바르지 않습니다.")
        String number
) {
}
