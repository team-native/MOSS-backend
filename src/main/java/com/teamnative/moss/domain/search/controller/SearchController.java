package com.teamnative.moss.domain.search.controller;

import com.teamnative.moss.domain.search.dto.PlaceSearchResponse;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.service.SearchService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

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
    @Operation(summary = "장소 이름 검색", description = "장소 이름에 검색어가 포함된 장소를 검색합니다.")
    public ApiResponse<PlaceSearchResponse> searchPlaces(
            @RequestParam @NotBlank @Size(max = 100) String query,
            @Valid @ModelAttribute PlaceSearchFilter filter
    ) {
        return ApiResponse.success("장소 검색 결과를 조회했습니다.", searchService.searchPlaces(query, filter));
    }
}
