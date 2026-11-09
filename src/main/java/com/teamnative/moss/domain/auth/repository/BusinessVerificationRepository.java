package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.BusinessVerification;
import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.dto.BusinessVerificationSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessVerificationRepository extends JpaRepository<BusinessVerification, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<BusinessVerification> findByUser_Id(Long userId);

    List<BusinessVerification> findByStatusOrderBySubmittedAtAsc(BusinessVerificationStatus status);

    List<BusinessVerification> findAllByOrderBySubmittedAtAsc();

    @Query("select new com.teamnative.moss.domain.auth.dto.BusinessVerificationSummary("
            + "verification.id, verification.user.name, verification.user.email, verification.registrationNumber, "
            + "verification.representativeName, verification.openingDate, verification.documentName, "
            + "verification.status, verification.reviewReason, verification.submittedAt, verification.updatedAt) "
            + "from BusinessVerification verification where verification.user.id = :userId")
    Optional<BusinessVerificationSummary> findSummaryByUserId(@Param("userId") Long userId);

    @Query("select new com.teamnative.moss.domain.auth.dto.BusinessVerificationSummary("
            + "verification.id, verification.user.name, verification.user.email, verification.registrationNumber, "
            + "verification.representativeName, verification.openingDate, verification.documentName, "
            + "verification.status, verification.reviewReason, verification.submittedAt, verification.updatedAt) "
            + "from BusinessVerification verification order by verification.submittedAt asc")
    List<BusinessVerificationSummary> findAllSummaries();

    @Query("select new com.teamnative.moss.domain.auth.dto.BusinessVerificationSummary("
            + "verification.id, verification.user.name, verification.user.email, verification.registrationNumber, "
            + "verification.representativeName, verification.openingDate, verification.documentName, "
            + "verification.status, verification.reviewReason, verification.submittedAt, verification.updatedAt) "
            + "from BusinessVerification verification where verification.status = :status "
            + "order by verification.submittedAt asc")
    List<BusinessVerificationSummary> findSummariesByStatus(@Param("status") BusinessVerificationStatus status);

    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);

    boolean existsByRegistrationNumber(String registrationNumber);
}
