package com.teamnative.moss.domain.review.repository;

import com.teamnative.moss.domain.review.entity.PlaceReview;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {
    long countByAuthor_IdAndDeletedAtIsNull(Long authorId);

    @EntityGraph(attributePaths = {"place", "photoUrls"})
    List<PlaceReview> findByAuthor_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long authorId);

    Optional<PlaceReview> findByAuthor_IdAndPlace_Id(Long authorId, Long placeId);

    @EntityGraph(attributePaths = {"place", "photoUrls"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select review from PlaceReview review where review.id = :reviewId and review.author.id = :authorId")
    Optional<PlaceReview> findByIdAndAuthorForUpdate(@Param("reviewId") Long reviewId,
                                                     @Param("authorId") Long authorId);

    List<PlaceReview> findByDeletedAtIsNotNullAndRestoreUntilLessThanEqual(LocalDateTime cutoff);
}
