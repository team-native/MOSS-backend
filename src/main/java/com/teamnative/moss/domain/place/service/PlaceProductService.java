package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.place.dto.PlaceProductRequest;
import com.teamnative.moss.domain.place.dto.PlaceProductResponse;
import com.teamnative.moss.domain.place.entity.PlaceProduct;
import com.teamnative.moss.domain.place.entity.ProductStatus;
import com.teamnative.moss.domain.place.repository.PlaceProductRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaceProductService {
    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;
    private final PlaceProductRepository productRepository;

    public PlaceProductService(UserRepository userRepository,
                               BusinessVerificationRepository verificationRepository,
                               PlaceRepository placeRepository,
                               PlaceProductRepository productRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceProductResponse> getBusinessProducts(String email, Long placeId) {
        requireApprovedBusiness(email);
        requirePlace(placeId);
        return productRepository.findByPlace_IdOrderBySortOrderAscIdAsc(placeId)
                .stream().map(PlaceProductResponse::from).toList();
    }

    @Transactional
    public PlaceProductResponse create(String email, Long placeId, PlaceProductRequest request) {
        requireApprovedBusiness(email);
        Place place = requirePlace(placeId);
        int sortOrder = request.sortOrder() == null ? nextSortOrder(placeId) : request.sortOrder();
        PlaceProduct product = new PlaceProduct(place, request.name().trim(), request.price(), request.status(),
                sortOrder, LocalDateTime.now());
        return PlaceProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public PlaceProductResponse update(String email, Long placeId, Long productId, PlaceProductRequest request) {
        requireApprovedBusiness(email);
        PlaceProduct product = productRepository.findByIdAndPlace_Id(productId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        int sortOrder = request.sortOrder() == null ? product.getSortOrder() : request.sortOrder();
        product.update(request.name().trim(), request.price(), request.status(), sortOrder, LocalDateTime.now());
        return PlaceProductResponse.from(product);
    }

    @Transactional
    public void delete(String email, Long placeId, Long productId) {
        requireApprovedBusiness(email);
        PlaceProduct product = productRepository.findByIdAndPlace_Id(productId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<PlaceProductResponse> getVisibleProducts(Long placeId) {
        Place place = requirePlace(placeId);
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다.");
        }
        return productRepository.findByPlace_IdAndStatusNotOrderBySortOrderAscIdAsc(placeId, ProductStatus.HIDDEN)
                .stream().map(PlaceProductResponse::from).toList();
    }

    private int nextSortOrder(Long placeId) {
        return productRepository.findFirstByPlace_IdOrderBySortOrderDesc(placeId)
                .map(product -> product.getSortOrder() + 1)
                .orElse(0);
    }

    private Place requirePlace(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
    }

    private UserAccount requireApprovedBusiness(String email) {
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
}
