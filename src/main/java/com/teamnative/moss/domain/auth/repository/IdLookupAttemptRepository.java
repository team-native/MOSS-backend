package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.IdLookupAttempt;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdLookupAttemptRepository extends JpaRepository<IdLookupAttempt, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from IdLookupAttempt attempt where attempt.lookupKey = :lookupKey")
    Optional<IdLookupAttempt> findByLookupKeyForUpdate(@Param("lookupKey") String lookupKey);

    @Modifying
    @Query("delete from IdLookupAttempt attempt where attempt.updatedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
