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

    @Column(name = "verification_attempts", nullable = false)
    private int verificationAttempts;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "reset_token_hash", length = 100)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String codeHash, LocalDateTime sentAt, LocalDateTime expiresAt) {
        this.email = email;
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.verificationAttempts = 0;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public void recordVerificationFailure() {
        verificationAttempts++;
    }

    public void markVerified(String resetTokenHash, LocalDateTime verifiedAt, LocalDateTime resetTokenExpiresAt) {
        this.verifiedAt = verifiedAt;
        this.resetTokenHash = resetTokenHash;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public void consumeResetToken() {
        this.resetTokenHash = null;
        this.resetTokenExpiresAt = null;
    }

    public void replaceCode(String codeHash, LocalDateTime sentAt, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.verificationAttempts = 0;
        this.verifiedAt = null;
        this.resetTokenHash = null;
        this.resetTokenExpiresAt = null;
    }
}
