package com.teamnative.moss.domain.place.entity;

import com.teamnative.moss.domain.search.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_photos", indexes = {
        @Index(name = "idx_place_photos_order", columnList = "place_id,is_representative,sort_order,id")
})
public class PlacePhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 30)
    private String contentType;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @Column(name = "is_representative", nullable = false)
    private boolean representative;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PlacePhoto() {}

    public PlacePhoto(Place place, String originalFilename, String contentType, byte[] imageData,
                      boolean representative, int sortOrder, LocalDateTime createdAt) {
        this.place = place;
        replaceImage(originalFilename, contentType, imageData);
        this.representative = representative;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public void replaceImage(String originalFilename, String contentType, byte[] imageData) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.imageData = imageData.clone();
    }

    public Long getId() { return id; }
    public Place getPlace() { return place; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public byte[] getImageData() { return imageData.clone(); }
    public boolean isRepresentative() { return representative; }
    public int getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
