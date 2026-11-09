package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.PlaceProductResponse;
import com.teamnative.moss.domain.place.service.PlaceProductService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/places/{placeId}/products")
@Tag(name = "Place Products")
public class PublicPlaceProductController {
    private final PlaceProductService productService;

    public PublicPlaceProductController(PlaceProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "고객용 상품 목록 조회", description = "숨김 상품은 제외하고 품절 상품은 포함해 반환합니다.")
    public ApiResponse<List<PlaceProductResponse>> getVisibleProducts(@PathVariable Long placeId) {
        return ApiResponse.success("상품 목록을 조회했습니다.", productService.getVisibleProducts(placeId));
    }
}
