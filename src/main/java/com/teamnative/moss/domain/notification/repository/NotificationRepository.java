package com.teamnative.moss.domain.notification.repository;

import com.teamnative.moss.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Query("delete from Notification notification where notification.user.id = :userId")
    int deleteAllForUser(@Param("userId") Long userId);

    Page<Notification> findByUser_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long userId, LocalDateTime cutoff, Pageable pageable);

    Page<Notification> findByUser_IdAndReadAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long userId, LocalDateTime cutoff, Pageable pageable);

    long countByUser_IdAndReadAtIsNullAndCreatedAtGreaterThanEqual(Long userId, LocalDateTime cutoff);

    Optional<Notification> findByIdAndUser_IdAndCreatedAtGreaterThanEqual(
            Long id, Long userId, LocalDateTime cutoff);

    @Modifying
    @Query("delete from Notification notification where notification.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("update Notification notification set notification.readAt = :readAt "
            + "where notification.user.id = :userId and notification.readAt is null "
            + "and notification.createdAt >= :cutoff")
    int markAllRead(@Param("userId") Long userId, @Param("cutoff") LocalDateTime cutoff,
                    @Param("readAt") LocalDateTime readAt);
}
