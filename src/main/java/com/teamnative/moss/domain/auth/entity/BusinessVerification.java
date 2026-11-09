package com.teamnative.moss.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_verifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_business_verification_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_business_verification_registration", columnNames = "registration_number")
})
public class BusinessVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "registration_number", nullable = false, length = 10)
    private String registrationNumber;

    @Column(name = "representative_name", nullable = false, length = 100)
    private String representativeName;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "document_content_type", nullable = false, length = 100)
    private String documentContentType;

    @Lob
    @Column(name = "document_data", nullable = false)
    private byte[] documentData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BusinessVerificationStatus status;

    @Column(name = "review_reason", length = 500)
    private String reviewReason;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BusinessVerification() {}

    public BusinessVerification(UserAccount user, String registrationNumber, String representativeName,
                                LocalDate openingDate, String documentName, String documentContentType,
                                byte[] documentData, LocalDateTime now) {
        this.user = user;
        replaceSubmission(registrationNumber, representativeName, openingDate, documentName,
                documentContentType, documentData, now);
    }

    public void replaceSubmission(String registrationNumber, String representativeName, LocalDate openingDate,
                                  String documentName, String documentContentType, byte[] documentData,
                                  LocalDateTime now) {
        this.registrationNumber = registrationNumber;
        this.representativeName = representativeName;
        this.openingDate = openingDate;
        this.documentName = documentName;
        this.documentContentType = documentContentType;
        this.documentData = documentData.clone();
        this.status = BusinessVerificationStatus.RECEIVED;
        this.reviewReason = null;
        this.submittedAt = now;
        this.updatedAt = now;
    }

    public void updateReview(BusinessVerificationStatus status, String reason, LocalDateTime now) {
        this.status = status;
        this.reviewReason = reason;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getRepresentativeName() { return representativeName; }
    public LocalDate getOpeningDate() { return openingDate; }
    public String getDocumentName() { return documentName; }
    public String getDocumentContentType() { return documentContentType; }
    public byte[] getDocumentData() { return documentData.clone(); }
    public BusinessVerificationStatus getStatus() { return status; }
    public String getReviewReason() { return reviewReason; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
