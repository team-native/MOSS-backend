package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.EmailVerification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select verification from EmailVerification verification where verification.email = :email")
    Optional<EmailVerification> findByEmailForUpdate(@Param("email") String email);
}
