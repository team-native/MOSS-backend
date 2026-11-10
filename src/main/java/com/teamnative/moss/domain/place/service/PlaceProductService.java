package com.teamnative.moss.domain.place.service;

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
    private final BusinessPlaceAccessService businessPlaceAccessService;
    private final PlaceRepository placeRepository;
    private final PlaceProductRepository productRepository;

    public PlaceProductService(BusinessPlaceAccessService businessPlaceAccessService,
                               PlaceRepository placeRepository,
                               PlaceProductRepository productRepository) {
        this.businessPlaceAccessService = businessPlaceAccessService;
        this.placeRepository = placeRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaceProductResponse> getBusinessProducts(String email, Long placeId) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        return productRepository.findByPlace_IdOrderBySortOrderAscIdAsc(placeId)
                .stream().map(PlaceProductResponse::from).toList();
    }

    @Transactional
    public PlaceProductResponse create(String email, Long placeId, PlaceProductRequest request) {
        Place place = businessPlaceAccessService.requireOwnedPlace(email, placeId);
        int sortOrder = request.sortOrder() == null ? nextSortOrder(placeId) : request.sortOrder();
        PlaceProduct product = new PlaceProduct(place, request.name().trim(), request.price(), request.status(),
                sortOrder, LocalDateTime.now());
        return PlaceProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public PlaceProductResponse update(String email, Long placeId, Long productId, PlaceProductRequest request) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
        PlaceProduct product = productRepository.findByIdAndPlace_Id(productId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        int sortOrder = request.sortOrder() == null ? product.getSortOrder() : request.sortOrder();
        product.update(request.name().trim(), request.price(), request.status(), sortOrder, LocalDateTime.now());
        return PlaceProductResponse.from(product);
    }

    @Transactional
    public void delete(String email, Long placeId, Long productId) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);
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

}
