package com.teamnative.moss.domain.notification.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Entity
@Table(name = "push_devices", uniqueConstraints =
        @UniqueConstraint(name = "uk_push_devices_token_hash", columnNames = "token_hash"), indexes =
        @Index(name = "idx_push_devices_user", columnList = "user_id"))
public class PushDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, columnDefinition = "text")
    private String token;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PushPlatform platform;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PushDevice() {}

    public PushDevice(UserAccount user, String token, PushPlatform platform, LocalDateTime now) {
        update(user, token, platform, now);
    }

    public void update(UserAccount user, String token, PushPlatform platform, LocalDateTime now) {
        this.user = user;
        this.token = token;
        this.tokenHash = hash(token);
        this.platform = platform;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getToken() { return token; }
    public PushPlatform getPlatform() { return platform; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
