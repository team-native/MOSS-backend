package com.teamnative.moss.domain.search.dto;

import com.teamnative.moss.domain.search.entity.RecentSearch;
import java.time.LocalDateTime;

public record RecentSearchResponse(Long id, String query, LocalDateTime searchedAt) {
    public static RecentSearchResponse from(RecentSearch search) {
        return new RecentSearchResponse(search.getId(), search.getQuery(), search.getCreatedAt());
    }
}
