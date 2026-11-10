package com.teamnative.moss.domain.search.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "recent_searches", uniqueConstraints =
        @UniqueConstraint(name = "uk_recent_search_user_query", columnNames = {"user_id", "query"}), indexes =
        @Index(name = "idx_recent_search_user_created", columnList = "user_id,created_at"))
public class RecentSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount user;

    @Column(nullable = false, length = 100)
    private String query;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RecentSearch() {}

    public RecentSearch(UserAccount user, String query) {
        this.user = user;
        this.query = query;
        this.createdAt = LocalDateTime.now();
    }

    public void touch() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getQuery() { return query; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
