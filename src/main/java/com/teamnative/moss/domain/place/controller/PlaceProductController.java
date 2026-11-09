package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceProductRequest;
import com.teamnative.moss.domain.place.dto.PlaceProductResponse;
import com.teamnative.moss.domain.place.service.PlaceProductService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/places/{placeId}/products")
@Tag(name = "Business Place Products")
public class PlaceProductController {
    private final PlaceProductService productService;

    public PlaceProductController(PlaceProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "업주 상품 목록 조회", description = "숨김 상품을 포함해 업주 매장의 상품을 정렬 순서대로 반환합니다.")
    public ApiResponse<List<PlaceProductResponse>> getBusinessProducts(
            @AuthenticationPrincipal String email, @PathVariable Long placeId) {
        return ApiResponse.success("상품 목록을 조회했습니다.", productService.getBusinessProducts(email, placeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "상품 등록", description = "매장에 메뉴 또는 이용권을 등록합니다.")
    public ApiResponse<PlaceProductResponse> create(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceProductRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "상품을 등록했습니다.",
                productService.create(email, placeId, request));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "상품명, 가격, 노출 상태와 정렬 순서를 수정합니다.")
    public ApiResponse<PlaceProductResponse> update(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId,
            @PathVariable Long productId,
            @Valid @RequestBody PlaceProductRequest request) {
        return ApiResponse.success("상품을 수정했습니다.",
                productService.update(email, placeId, productId, request));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "매장의 상품을 삭제합니다.")
    public ApiResponse<Void> delete(@AuthenticationPrincipal String email,
                                    @PathVariable Long placeId,
                                    @PathVariable Long productId) {
        productService.delete(email, placeId, productId);
        return ApiResponse.success("상품을 삭제했습니다.", null);
    }
}
