package com.teamnative.moss.domain.report.repository;

import com.teamnative.moss.domain.report.entity.ReportType;
import com.teamnative.moss.domain.report.entity.InfoIssueType;
import com.teamnative.moss.domain.report.entity.ReportStatus;
import com.teamnative.moss.domain.report.entity.UserReport;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    long countByReporter_Id(Long reporterId);

    @EntityGraph(attributePaths = "photoUrls")
    List<UserReport> findByReporter_IdOrderByCreatedAtDesc(Long reporterId);

    @EntityGraph(attributePaths = "photoUrls")
    List<UserReport> findByReporter_IdAndReportTypeOrderByCreatedAtDesc(Long reporterId, ReportType reportType);

    @EntityGraph(attributePaths = "photoUrls")
    Optional<UserReport> findByIdAndReporter_Id(Long id, Long reporterId);

    boolean existsByReportNumber(String reportNumber);

    boolean existsByPlace_IdAndReportTypeAndIssueTypeAndStatusIn(
            Long placeId, ReportType reportType, InfoIssueType issueType, Collection<ReportStatus> statuses);

    boolean existsByReporter_IdAndReview_Id(Long reporterId, Long reviewId);
}
