package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.StoreInformationRequest;
import com.teamnative.moss.domain.place.dto.StoreInformationResponse;
import com.teamnative.moss.domain.place.service.StoreInformationService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/places")
@Tag(name = "Business Places")
public class StoreInformationController {
    private final StoreInformationService storeInformationService;

    public StoreInformationController(StoreInformationService storeInformationService) {
        this.storeInformationService = storeInformationService;
    }

    @GetMapping("/{placeId}")
    @Operation(summary = "업주 매장 정보 조회", description = "승인된 업주가 매장 정보를 조회합니다.")
    public ApiResponse<StoreInformationResponse> information(@AuthenticationPrincipal String email,
                                                              @PathVariable Long placeId) {
        return ApiResponse.success("매장 정보를 조회했습니다.",
                storeInformationService.getInformation(email, placeId));
    }

    @PutMapping("/{placeId}")
    @Operation(summary = "업주 매장 정보 수정", description = "승인된 업주가 매장 정보를 수정하고 변경 이력을 저장합니다.")
    public ApiResponse<StoreInformationResponse> updateInformation(
            @AuthenticationPrincipal String email,
            @PathVariable Long placeId,
            @Valid @RequestBody StoreInformationRequest request) {
        return ApiResponse.success("매장 정보를 수정했습니다.",
                storeInformationService.updateInformation(email, placeId, request));
    }
}
