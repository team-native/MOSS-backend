package com.teamnative.moss.domain.report.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.entity.PlaceType;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.review.entity.PlaceReview;
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
@Table(name = "user_reports", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_reports_number", columnNames = "report_number"),
        @UniqueConstraint(name = "uk_user_reports_reporter_review", columnNames = {"reporter_id", "review_id"})
}, indexes = {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private PlaceReview review;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 30)
    private InfoIssueType issueType;

    @Column(length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_report_reason", length = 30)
    private ReviewReportReason reviewReportReason;

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

    public UserReport(UserAccount reporter, Place place, InfoIssueType issueType, String content,
                      List<String> photoUrls, LocalDateTime now) {
        this.reporter = reporter;
        this.place = place;
        this.reportType = ReportType.INFO_ERROR;
        this.status = ReportStatus.RECEIVED;
        this.placeName = place.getName();
        this.address = place.getAddress();
        this.placeType = place.getPlaceType();
        this.issueType = issueType;
        this.content = content;
        this.photoUrls = new ArrayList<>(photoUrls);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UserReport(UserAccount reporter, PlaceReview review, ReviewReportReason reason,
                      String detail, LocalDateTime now) {
        this.reporter = reporter;
        this.review = review;
        this.place = review.getPlace();
        this.reportType = ReportType.REVIEW;
        this.status = ReportStatus.RECEIVED;
        this.placeName = review.getPlace().getName();
        this.address = review.getPlace().getAddress();
        this.placeType = review.getPlace().getPlaceType();
        this.reviewReportReason = reason;
        this.content = detail;
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
    public UserAccount getReporter() { return reporter; }
    public String getReportNumber() { return reportNumber; }
    public ReportType getReportType() { return reportType; }
    public ReportStatus getStatus() { return status; }
    public String getPlaceName() { return placeName; }
    public String getAddress() { return address; }
    public PlaceType getPlaceType() { return placeType; }
    public Place getPlace() { return place; }
    public PlaceReview getReview() { return review; }
    public InfoIssueType getIssueType() { return issueType; }
    public ReviewReportReason getReviewReportReason() { return reviewReportReason; }
    public String getContent() { return content; }
    public String getRecommendationReason() { return recommendationReason; }
    public List<String> getPhotoUrls() { return List.copyOf(photoUrls); }
    public String getResultReason() { return resultReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
