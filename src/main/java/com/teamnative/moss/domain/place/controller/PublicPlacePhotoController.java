package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlacePhotoListResponse;
import com.teamnative.moss.domain.place.service.PlacePhotoService;
import com.teamnative.moss.domain.place.service.PlacePhotoService.PhotoData;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/places/{placeId}/photos")
@Tag(name = "Place Photos")
public class PublicPlacePhotoController {
    private final PlacePhotoService photoService;

    public PublicPlacePhotoController(PlacePhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    @Operation(summary = "고객용 매장 사진 목록", description = "공개 매장의 대표 사진과 추가 사진 목록을 반환합니다.")
    public ApiResponse<PlacePhotoListResponse> getPhotos(@PathVariable Long placeId) {
        return ApiResponse.success("매장 사진을 조회했습니다.", photoService.getPublicPhotos(placeId));
    }

    @GetMapping("/{photoId}")
    @Operation(summary = "매장 사진 파일 조회", description = "공개 매장에 등록된 사진 파일을 반환합니다.")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long placeId, @PathVariable Long photoId) {
        PhotoData photo = photoService.getPublicPhoto(placeId, photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff")
                .body(photo.data());
    }
}
