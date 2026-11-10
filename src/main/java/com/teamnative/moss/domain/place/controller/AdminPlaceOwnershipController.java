package com.teamnative.moss.domain.place.controller;

import com.teamnative.moss.domain.place.dto.AssignPlaceOwnerRequest;
import com.teamnative.moss.domain.place.service.PlaceOwnershipService;
import com.teamnative.moss.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/places")
@Tag(name = "Admin Place Ownership")
public class AdminPlaceOwnershipController {
    private final PlaceOwnershipService ownershipService;

    public AdminPlaceOwnershipController(PlaceOwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @PatchMapping("/{placeId}/owner")
    @Operation(summary = "매장 소유자 지정", description = "관리자가 인증 승인된 업주를 기존 매장의 소유자로 지정합니다.")
    public ApiResponse<Void> assignOwner(@AuthenticationPrincipal String email,
                                         @PathVariable Long placeId,
                                         @Valid @RequestBody AssignPlaceOwnerRequest request) {
        ownershipService.assignOwner(email, placeId, request.businessUserId());
        return ApiResponse.success("매장 소유자를 지정했습니다.", null);
    }
}
