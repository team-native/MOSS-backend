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
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessPlaceAccessService {
    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;

    public BusinessPlaceAccessService(UserRepository userRepository,
                                      BusinessVerificationRepository verificationRepository,
                                      PlaceRepository placeRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
    }

    public Place requireOwnedPlace(String email, Long placeId) {
        UserAccount business = requireApprovedBusiness(email);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        requireOwner(business, place);
        return place;
    }

    public Place requireOwnedPlace(String email, Place place) {
        UserAccount business = requireApprovedBusiness(email);
        requireOwner(business, place);
        return place;
    }

    public UserAccount requireApprovedBusiness(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "업주 계정만 이용할 수 있습니다.");
        }
        boolean approved = verificationRepository.findByUser_Id(user.getId())
                .map(verification -> verification.getStatus() == BusinessVerificationStatus.APPROVED)
                .orElse(false);
        if (!approved) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사업자 인증 승인 후 이용할 수 있습니다.");
        }
        return user;
    }

    private void requireOwner(UserAccount business, Place place) {
        UserAccount owner = place.getOwner();
        if (owner == null || !owner.getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 매장을 관리할 권한이 없습니다.");
        }
    }
}
