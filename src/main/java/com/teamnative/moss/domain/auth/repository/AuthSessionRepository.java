package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
    @Modifying
    @Query("update AuthSession session set session.revokedAt = :now where session.userId = :userId and session.revokedAt is null")
    int revokeAllForUser(@Param("userId") Long userId, @Param("now") java.time.Instant now);
}
