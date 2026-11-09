package com.teamnative.moss.domain.favorite.controller;

import com.teamnative.moss.domain.favorite.dto.FavoriteListResponse;
import com.teamnative.moss.domain.favorite.dto.FavoriteMutationResponse;
import com.teamnative.moss.domain.favorite.service.FavoriteService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/favorites")
@Tag(name = "Favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "찜 목록 조회", description = "로그인한 사용자의 찜 목록을 기준 좌표와 가까운 순으로 반환합니다.")
    public ApiResponse<FavoriteListResponse> getFavorites(
            @AuthenticationPrincipal String email,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double longitude
    ) {
        return ApiResponse.success("찜 목록을 조회했습니다.", favoriteService.getFavorites(email, latitude, longitude));
    }

    @PostMapping("/{placeId}")
    @Operation(summary = "장소 찜 추가", description = "중복과 최대 30개 제한을 확인한 뒤 장소를 찜합니다.")
    public ApiResponse<FavoriteMutationResponse> addFavorite(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId
    ) {
        return ApiResponse.success("장소를 찜 목록에 추가했습니다.", favoriteService.addFavorite(email, placeId));
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "장소 찜 해제", description = "5초 동안 되돌릴 수 있도록 찜을 해제합니다.")
    public ApiResponse<FavoriteMutationResponse> removeFavorite(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId
    ) {
        return ApiResponse.success("장소를 찜 목록에서 해제했습니다.", favoriteService.removeFavorite(email, placeId));
    }

    @PostMapping("/{placeId}/restore")
    @Operation(summary = "찜 해제 되돌리기", description = "해제 후 5초 이내에 찜을 복원합니다.")
    public ApiResponse<FavoriteMutationResponse> restoreFavorite(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId
    ) {
        return ApiResponse.success("찜을 되돌렸습니다.", favoriteService.restoreFavorite(email, placeId));
    }
}
