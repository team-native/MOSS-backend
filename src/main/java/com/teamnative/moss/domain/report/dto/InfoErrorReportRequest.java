package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.report.entity.InfoIssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record InfoErrorReportRequest(
        @NotNull(message = "장소를 선택해 주세요.")
        Long placeId,

        @NotNull(message = "오류 항목을 선택해 주세요.")
        InfoIssueType issueType,

        @NotBlank(message = "오류 내용을 입력해 주세요.")
        @Size(min = 10, max = 500, message = "오류 내용은 10자 이상 500자 이하여야 합니다.")
        String content,

        @Size(max = 3, message = "사진은 최대 3장까지 첨부할 수 있습니다.")
        List<@NotBlank(message = "사진 주소는 비워둘 수 없습니다.") @Size(max = 2048) String> photoUrls
) {
    public InfoErrorReportRequest {
        photoUrls = photoUrls == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(photoUrls));
    }
}
