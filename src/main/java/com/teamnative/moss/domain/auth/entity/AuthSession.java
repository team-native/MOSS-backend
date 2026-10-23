package com.teamnative.moss.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected AuthSession() {}

    public AuthSession(String sessionId, Long userId, Instant expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public boolean isActiveAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public Long getUserId() {
        return userId;
    }

    public void extendUntil(Instant newExpiresAt) {
        this.expiresAt = newExpiresAt;
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }
}
