package com.teamnative.moss.domain.review.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.dto.PlaceSearchFilter;
import com.teamnative.moss.domain.search.entity.Place;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "place_reviews", uniqueConstraints = @UniqueConstraint(
        name = "uk_place_reviews_author_place", columnNames = {"author_id", "place_id"}), indexes = {
        @Index(name = "idx_place_reviews_author_created", columnList = "author_id,created_at"),
        @Index(name = "idx_place_reviews_deleted_restore", columnList = "deleted_at,restore_until")
})
public class PlaceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserAccount author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "noise_level", nullable = false, length = 20)
    private PlaceSearchFilter.NoiseLevel noiseLevel;

    @Column(nullable = false, length = 500)
    private String content;

    @ElementCollection
    @CollectionTable(name = "place_review_photos", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "photo_order")
    @Column(name = "photo_url", nullable = false, length = 2048)
    private List<String> photoUrls = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "restore_until")
    private LocalDateTime restoreUntil;

    @Column(name = "business_reply", length = 500)
    private String businessReply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_reply_author_id")
    private UserAccount businessReplyAuthor;

    @Column(name = "business_reply_created_at")
    private LocalDateTime businessReplyCreatedAt;

    @Column(name = "business_reply_updated_at")
    private LocalDateTime businessReplyUpdatedAt;

    protected PlaceReview() {
    }

    public PlaceReview(UserAccount author, Place place, BigDecimal rating,
                       PlaceSearchFilter.NoiseLevel noiseLevel, String content,
                       List<String> photoUrls, LocalDateTime now) {
        this.author = author;
        this.place = place;
        this.rating = rating;
        this.noiseLevel = noiseLevel;
        this.content = content;
        this.photoUrls = new ArrayList<>(photoUrls);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canRestoreAt(LocalDateTime now) {
        return isDeleted() && restoreUntil != null && !now.isAfter(restoreUntil);
    }

    public void update(BigDecimal rating, PlaceSearchFilter.NoiseLevel noiseLevel,
                       String content, List<String> photoUrls, LocalDateTime now) {
        this.rating = rating;
        this.noiseLevel = noiseLevel;
        this.content = content;
        this.photoUrls.clear();
        this.photoUrls.addAll(photoUrls);
        this.updatedAt = now;
        this.lastEditedAt = now;
    }

    public LocalDateTime softDelete(LocalDateTime now) {
        this.deletedAt = now;
        this.restoreUntil = now.plusDays(7);
        this.updatedAt = now;
        return restoreUntil;
    }

    public void restore(LocalDateTime now) {
        this.deletedAt = null;
        this.restoreUntil = null;
        this.updatedAt = now;
    }

    public void addBusinessReply(UserAccount author, String content, LocalDateTime now) {
        this.businessReplyAuthor = author;
        this.businessReply = content;
        this.businessReplyCreatedAt = now;
        this.businessReplyUpdatedAt = now;
    }

    public void updateBusinessReply(String content, LocalDateTime now) {
        this.businessReply = content;
        this.businessReplyUpdatedAt = now;
    }

    public void deleteBusinessReply() {
        this.businessReply = null;
        this.businessReplyAuthor = null;
        this.businessReplyCreatedAt = null;
        this.businessReplyUpdatedAt = null;
    }

    public Long getId() { return id; }
    public UserAccount getAuthor() { return author; }
    public Place getPlace() { return place; }
    public BigDecimal getRating() { return rating; }
    public PlaceSearchFilter.NoiseLevel getNoiseLevel() { return noiseLevel; }
    public String getContent() { return content; }
    public List<String> getPhotoUrls() { return List.copyOf(photoUrls); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastEditedAt() { return lastEditedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getRestoreUntil() { return restoreUntil; }
    public String getBusinessReply() { return businessReply; }
    public UserAccount getBusinessReplyAuthor() { return businessReplyAuthor; }
    public LocalDateTime getBusinessReplyCreatedAt() { return businessReplyCreatedAt; }
    public LocalDateTime getBusinessReplyUpdatedAt() { return businessReplyUpdatedAt; }
}
