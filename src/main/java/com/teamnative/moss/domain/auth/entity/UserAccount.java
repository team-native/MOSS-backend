package com.teamnative.moss.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_number", columnNames = "phone_number")
})
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String number;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "login_locked_until")
    private LocalDateTime loginLockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private Grade grade;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected UserAccount() {
    }

    public UserAccount(
            Role role,
            String email,
            String name,
            String number,
            String passwordHash,
            Grade grade
    ) {
        this.role = role;
        this.email = email;
        this.name = name;
        this.number = number;
        this.passwordHash = passwordHash;
        this.grade = grade;
        this.createdAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }

    public boolean isPendingDeletion() { return deletedAt != null; }

    public void requestDeletion(LocalDateTime now) {
        if (deletedAt == null) deletedAt = now;
    }

    public void restore() { deletedAt = null; }

    public boolean isLoginLockedAt(LocalDateTime now) {
        return loginLockedUntil != null && loginLockedUntil.isAfter(now);
    }

    public boolean clearExpiredLoginLock(LocalDateTime now) {
        if (loginLockedUntil == null || loginLockedUntil.isAfter(now)) {
            return false;
        }
        resetFailedLogins();
        return true;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void recordFailedLogin(LocalDateTime now, int maxAttempts, long lockMinutes) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) {
            loginLockedUntil = now.plusMinutes(lockMinutes);
        }
    }

    public void resetFailedLogins() {
        failedLoginAttempts = 0;
        loginLockedUntil = null;
    }
}
