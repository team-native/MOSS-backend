package com.teamnative.moss.domain.report.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.entity.PlaceType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_user_reports_number", columnNames = "report_number"), indexes = {
        @Index(name = "idx_user_reports_reporter_created", columnList = "reporter_id,created_at")
})
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_number", length = 12, unique = true)
    private String reportNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserAccount reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "place_name", length = 150)
    private String placeName;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", length = 30)
    private PlaceType placeType;

    @Column(name = "recommendation_reason", length = 500)
    private String recommendationReason;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_report_photos", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "photo_order")
    @Column(name = "photo_url", nullable = false, length = 2048)
    private List<String> photoUrls = new ArrayList<>();

    @Column(name = "result_reason", length = 500)
    private String resultReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserReport() {
    }

    public UserReport(UserAccount reporter, ReportType reportType, String placeName, String address,
                      PlaceType placeType, String recommendationReason, List<String> photoUrls,
                      LocalDateTime now) {
        this.reporter = reporter;
        this.reportType = reportType;
        this.placeName = placeName;
        this.address = address;
        this.placeType = placeType;
        this.recommendationReason = recommendationReason;
        this.photoUrls = new ArrayList<>(photoUrls);
        this.status = ReportStatus.RECEIVED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void assignReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public void updateProcessing(ReportStatus status, String resultReason, LocalDateTime now) {
        this.status = status;
        this.resultReason = resultReason;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getReportNumber() { return reportNumber; }
    public ReportType getReportType() { return reportType; }
    public ReportStatus getStatus() { return status; }
    public String getPlaceName() { return placeName; }
    public String getAddress() { return address; }
    public PlaceType getPlaceType() { return placeType; }
    public String getRecommendationReason() { return recommendationReason; }
    public List<String> getPhotoUrls() { return List.copyOf(photoUrls); }
    public String getResultReason() { return resultReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
