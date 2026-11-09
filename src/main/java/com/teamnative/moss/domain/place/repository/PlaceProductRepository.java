package com.teamnative.moss.domain.place.repository;

import com.teamnative.moss.domain.place.entity.PlaceProduct;
import com.teamnative.moss.domain.place.entity.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceProductRepository extends JpaRepository<PlaceProduct, Long> {
    List<PlaceProduct> findByPlace_IdOrderBySortOrderAscIdAsc(Long placeId);

    List<PlaceProduct> findByPlace_IdAndStatusNotOrderBySortOrderAscIdAsc(Long placeId, ProductStatus status);

    Optional<PlaceProduct> findByIdAndPlace_Id(Long productId, Long placeId);

    Optional<PlaceProduct> findFirstByPlace_IdOrderBySortOrderDesc(Long placeId);
}
