package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlacePhotoListResponse;
import com.teamnative.moss.domain.place.dto.PlacePhotoResponse;
import com.teamnative.moss.domain.place.dto.PlaceVisibilityRequest;
import com.teamnative.moss.domain.place.service.PlacePhotoService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/business/places/{placeId}")
@Tag(name = "Business Place Photos")
public class BusinessPlacePhotoController {
    private final PlacePhotoService photoService;

    public BusinessPlacePhotoController(PlacePhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/photos")
    @Operation(summary = "업주 매장 사진 조회", description = "대표 사진과 매장 사진, 매장 공개 상태를 조회합니다.")
    public ApiResponse<PlacePhotoListResponse> getPhotos(@AuthenticationPrincipal String email,
                                                         @PathVariable Long placeId) {
        return ApiResponse.success("매장 사진을 조회했습니다.", photoService.getBusinessPhotos(email, placeId));
    }

    @PutMapping(value = "/photos/representative", consumes = "multipart/form-data")
    @Operation(summary = "대표 사진 등록·변경", description = "대표 사진이 있으면 교체하고 없으면 등록합니다.")
    public ApiResponse<PlacePhotoResponse> replaceRepresentative(@AuthenticationPrincipal String email,
                                                                 @PathVariable Long placeId,
                                                                 @RequestParam("photo") MultipartFile photo) {
        return ApiResponse.success("대표 사진을 저장했습니다.",
                photoService.replaceRepresentative(email, placeId, photo));
    }

    @PostMapping(value = "/photos", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "매장 사진 추가", description = "대표 사진 외 매장 사진을 추가합니다.")
    public ApiResponse<PlacePhotoResponse> addPhoto(@AuthenticationPrincipal String email,
                                                    @PathVariable Long placeId,
                                                    @RequestParam("photo") MultipartFile photo) {
        return ApiResponse.success(HttpStatus.CREATED, "매장 사진을 등록했습니다.",
                photoService.addPhoto(email, placeId, photo));
    }

    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "매장 사진 삭제", description = "대표 사진 외 매장 사진을 삭제합니다.")
    public ApiResponse<Void> deletePhoto(@AuthenticationPrincipal String email,
                                         @PathVariable Long placeId,
                                         @PathVariable Long photoId) {
        photoService.deletePhoto(email, placeId, photoId);
        return ApiResponse.success("매장 사진을 삭제했습니다.", null);
    }

    @PatchMapping("/visibility")
    @Operation(summary = "매장 공개 상태 변경", description = "매장을 공개하거나 숨김 상태로 전환합니다.")
    public ApiResponse<Boolean> updateVisibility(@AuthenticationPrincipal String email,
                                                @PathVariable Long placeId,
                                                @Valid @RequestBody PlaceVisibilityRequest request) {
        boolean publiclyVisible = photoService.updateVisibility(email, placeId, request.publiclyVisible());
        return ApiResponse.success("매장 공개 상태를 변경했습니다.", publiclyVisible);
    }
}
