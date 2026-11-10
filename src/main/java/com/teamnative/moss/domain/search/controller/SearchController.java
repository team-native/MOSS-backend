package com.teamnative.moss.domain.search.controller;

import com.teamnative.moss.domain.search.dto.PlaceSearchResponse;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.dto.RecentSearchResponse;
import com.teamnative.moss.domain.search.service.SearchService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Validated
@RestController
@RequestMapping("/search")
@Tag(name = "Search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/places")
    @Operation(summary = "장소 이름 검색", description = "장소 이름에 검색어가 포함된 장소를 검색합니다. 성공한 검색어는 최근 검색어에 저장됩니다.")
    public ApiResponse<PlaceSearchResponse> searchPlaces(
            @AuthenticationPrincipal String email,
            @RequestParam @NotBlank @Size(max = 100) String query,
            @Valid @ModelAttribute PlaceSearchFilter filter
    ) {
        return ApiResponse.success("장소 검색 결과를 조회했습니다.", searchService.searchPlaces(email, query, filter));
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 검색어 조회", description = "최근 검색어 최대 10개를 최신순으로 조회합니다.")
    public ApiResponse<java.util.List<RecentSearchResponse>> getRecentSearches(
            @AuthenticationPrincipal String email) {
        return ApiResponse.success("최근 검색어를 조회했습니다.", searchService.getRecentSearches(email));
    }

    @DeleteMapping("/recent/{searchId}")
    @Operation(summary = "최근 검색어 개별 삭제")
    public ApiResponse<Void> deleteRecentSearch(@AuthenticationPrincipal String email,
                                                @PathVariable Long searchId) {
        searchService.deleteRecentSearch(email, searchId);
        return ApiResponse.success("최근 검색어를 삭제했습니다.", null);
    }

    @DeleteMapping("/recent")
    @Operation(summary = "최근 검색어 전체 삭제")
    public ApiResponse<Void> deleteAllRecentSearches(@AuthenticationPrincipal String email) {
        searchService.deleteAllRecentSearches(email);
        return ApiResponse.success("최근 검색어를 모두 삭제했습니다.", null);
    }
}
