package com.teamnative.moss.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "id_lookup_attempts")
public class IdLookupAttempt {
    @Id
    @Column(name = "lookup_key", length = 64, nullable = false)
    private String lookupKey;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected IdLookupAttempt() {}

    public IdLookupAttempt(String lookupKey, LocalDateTime now) {
        this.lookupKey = lookupKey;
        this.updatedAt = now;
    }

    public void recordFailure(LocalDateTime now, int maximumAttempts, long lockMinutes) {
        failedAttempts++;
        if (failedAttempts >= maximumAttempts) {
            lockedUntil = now.plusMinutes(lockMinutes);
        }
        updatedAt = now;
    }

    public void reset(LocalDateTime now) {
        failedAttempts = 0;
        lockedUntil = null;
        updatedAt = now;
    }

    public void clearExpiredLock(LocalDateTime now) {
        if (lockedUntil != null && !lockedUntil.isAfter(now)) {
            reset(now);
        }
    }

    public String getLookupKey() { return lookupKey; }
    public int getFailedAttempts() { return failedAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
