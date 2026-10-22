package com.teamnative.moss.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {
    @Id
    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String codeHash, LocalDateTime sentAt, LocalDateTime expiresAt) {
        this.email = email;
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void replaceCode(String codeHash, LocalDateTime sentAt, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
    }
}
