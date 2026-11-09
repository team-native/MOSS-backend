package com.teamnative.moss.domain.report.dto;

import com.teamnative.moss.domain.search.entity.PlaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record StudySpotReportRequest(
        @NotBlank(message = "장소명을 입력해 주세요.")
        @Size(max = 150, message = "장소명은 150자 이하여야 합니다.")
        String placeName,

        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다.")
        String address,

        @NotNull(message = "장소 유형을 선택해 주세요.")
        PlaceType placeType,

        @Size(max = 500, message = "추천 이유는 500자 이하여야 합니다.")
        String recommendationReason,

        @Size(max = 3, message = "사진은 최대 3장까지 첨부할 수 있습니다.")
        List<@NotBlank(message = "사진 주소는 비워둘 수 없습니다.") @Size(max = 2048) String> photoUrls
) {
    public StudySpotReportRequest {
        photoUrls = photoUrls == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(photoUrls));
    }
}
