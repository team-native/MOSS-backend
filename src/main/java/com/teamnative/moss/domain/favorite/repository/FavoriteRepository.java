package com.teamnative.moss.domain.favorite.repository;

import com.teamnative.moss.domain.favorite.entity.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Modifying
    @Query("delete from Favorite favorite where favorite.user.id = :userId")
    int deleteAllForUser(@Param("userId") Long userId);

    long countByUser_IdAndRemovedAtIsNull(Long userId);

    boolean existsByUser_IdAndPlace_IdAndRemovedAtIsNull(Long userId, Long placeId);

    @EntityGraph(attributePaths = "place")
    List<Favorite> findByUser_IdAndRemovedAtIsNull(Long userId);

    Optional<Favorite> findByUser_IdAndPlace_Id(Long userId, Long placeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select favorite from Favorite favorite where favorite.user.id = :userId and favorite.place.id = :placeId")
    Optional<Favorite> findByUserAndPlaceForUpdate(@Param("userId") Long userId, @Param("placeId") Long placeId);
}
