package com.teamnative.moss.domain.review.dto;

import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ReviewRequest(
        @NotNull(message = "별점을 입력해 주세요.")
        @DecimalMin(value = "0.5", message = "별점은 0.5점 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5점 이하여야 합니다.")
        BigDecimal rating,

        @NotNull(message = "소음 체감을 선택해 주세요.")
        PlaceSearchFilter.NoiseLevel noiseLevel,

        @NotBlank(message = "리뷰 내용을 입력해 주세요.")
        @Size(max = 500, message = "리뷰는 500자 이하여야 합니다.")
        String content,

        @Size(max = 3, message = "사진은 최대 3장까지 첨부할 수 있습니다.")
        List<@NotBlank(message = "사진 주소는 비워둘 수 없습니다.") @Size(max = 2048, message = "사진 주소가 너무 깁니다.") String> photoUrls
) {
    public ReviewRequest {
        photoUrls = photoUrls == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(photoUrls));
    }
}
