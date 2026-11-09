package com.teamnative.moss.domain.place.entity;

import com.teamnative.moss.domain.search.entity.Place;
import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_products", indexes = {
        @Index(name = "idx_place_products_order", columnList = "place_id,sort_order,id")
})
public class PlaceProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PlaceProduct() {}

    public PlaceProduct(Place place, String name, int price, ProductStatus status, int sortOrder,
                        LocalDateTime now) {
        this.place = place;
        update(name, price, status, sortOrder, now);
        this.createdAt = now;
    }

    public void update(String name, int price, ProductStatus status, int sortOrder, LocalDateTime now) {
        this.name = name;
        this.price = price;
        this.status = status;
        this.sortOrder = sortOrder;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public Place getPlace() { return place; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public int getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
