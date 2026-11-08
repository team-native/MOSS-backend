package com.teamnative.moss.domain.favorite.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(
        name = "uk_favorites_user_place", columnNames = {"user_id", "place_id"}), indexes = {
        @Index(name = "idx_favorites_user_active", columnList = "user_id,removed_at,created_at")
})
public class Favorite {
    private static final long UNDO_WINDOW_SECONDS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "undo_expires_at")
    private LocalDateTime undoExpiresAt;

    protected Favorite() {
    }

    public Favorite(UserAccount user, Place place, LocalDateTime now) {
        this.user = user;
        this.place = place;
        this.createdAt = now;
    }

    public boolean isActive() {
        return removedAt == null;
    }

    public LocalDateTime remove(LocalDateTime now) {
        removedAt = now;
        undoExpiresAt = now.plusSeconds(UNDO_WINDOW_SECONDS);
        return undoExpiresAt;
    }

    public boolean restore(LocalDateTime now) {
        if (isActive() || undoExpiresAt == null || now.isAfter(undoExpiresAt)) {
            return false;
        }
        removedAt = null;
        undoExpiresAt = null;
        return true;
    }

    public void reactivate(LocalDateTime now) {
        removedAt = null;
        undoExpiresAt = null;
        createdAt = now;
    }

    public Long getId() { return id; }
    public Place getPlace() { return place; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUndoExpiresAt() { return undoExpiresAt; }
}
