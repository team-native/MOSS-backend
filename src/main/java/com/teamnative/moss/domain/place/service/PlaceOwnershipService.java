package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaceOwnershipService {
    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;

    public PlaceOwnershipService(UserRepository userRepository,
                                 BusinessVerificationRepository verificationRepository,
                                 PlaceRepository placeRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
    }

    @Transactional
    public void assignOwner(String adminEmail, Long placeId, Long businessUserId) {
        UserAccount admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 매장 소유자를 지정할 수 있습니다.");
        }

        UserAccount business = userRepository.findById(businessUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "업주 계정을 찾을 수 없습니다."));
        if (business.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자 계정만 매장 소유자로 지정할 수 있습니다.");
        }
        boolean approved = verificationRepository.findByUser_Id(business.getId())
                .map(verification -> verification.getStatus() == BusinessVerificationStatus.APPROVED)
                .orElse(false);
        if (!approved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "사업자 인증이 승인된 계정만 지정할 수 있습니다.");
        }

        Place place = placeRepository.findByIdForOwnershipUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        UserAccount existingOwner = place.getOwner();
        if (existingOwner != null && !existingOwner.getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 다른 소유자가 지정된 매장입니다.");
        }
        place.assignOwner(business);
    }
}
