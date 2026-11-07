package com.teamnative.moss.domain.search.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

public record PlaceSearchFilter(
        @Min(0) @Max(20_000) Integer price, // 시간당가격
        NoiseLevel noiseLevel, // 소음 단계
        Integer distance, // 거리
        @DecimalMin("-90") @DecimalMax("90") Double latitude, // 위도
        @DecimalMin("-180") @DecimalMax("180") Double longitude, // 경도
        Set<Facility> facilities // 편의시설
) {
    public PlaceSearchFilter {
        facilities = facilities == null ? Set.of() : Set.copyOf(facilities);
        if (distance != null && distance != 200 && distance != 500 && distance != 1_000) {
            throw new IllegalArgumentException("거리는 200, 500, 1000미터 중 하나여야 합니다.");
        }
        if ((distance == null) != (latitude == null || longitude == null)) {
            throw new IllegalArgumentException("거리 필터에는 기준 좌표 latitude와 longitude가 모두 필요합니다.");
        }
    }

    public enum NoiseLevel {
        QUIET, // 조용함
        NORMAL, // 보통
        TALK // 대화가능
    }

    public enum Facility {
        OUTLETS, // 콘센트 많음
        WIFI, // 와이파이
        OUTSIDE_FOOD, // 외부 음식 취사 가능
        STUDY_ROOM, // 스터디룸 보유
        LAPTOP, // 노트북 타이핑 가능
        CALL_ROOM // 통화룸 보유
    }
}
