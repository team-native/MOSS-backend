package com.teamnative.moss.domain.search.service;

import com.teamnative.moss.domain.search.dto.PlaceSearchResponse;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private static final Pattern NON_LETTER_OR_DIGIT = Pattern.compile("[^\\p{L}\\p{N}]");

    public PlaceSearchResponse searchPlaces(String rawQuery) {
        return searchPlaces(rawQuery, new PlaceSearchFilter(null, null, null, null, null, null));
    }

    public PlaceSearchResponse searchPlaces(String rawQuery, PlaceSearchFilter filter) {
        String query = normalize(rawQuery);
        // Apply filter to the place search provider/repository when the place catalog is introduced.
        return new PlaceSearchResponse(query, List.of());
    }

    static String normalize(String rawQuery) {
        if (rawQuery == null) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        String query = NON_LETTER_OR_DIGIT.matcher(rawQuery.trim()).replaceAll("");
        if (query.isBlank()) {
            throw new IllegalArgumentException("검색어에 문자 또는 숫자를 입력해주세요.");
        }
        return query;
    }
}
