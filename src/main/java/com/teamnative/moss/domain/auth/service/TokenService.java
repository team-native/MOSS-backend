package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.security.MessageDigest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final byte[] secret;
    private final ObjectMapper objectMapper;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public TokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-seconds:900}") long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-expiration-seconds:1209600}") long refreshTokenExpirationSeconds,
            ObjectMapper objectMapper
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes.");
        }
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        this.objectMapper = objectMapper;
    }

    public String createAccessToken(UserAccount user) {
        return createToken(user, "access", accessTokenExpirationSeconds);
    }

    public String createRefreshToken(UserAccount user) {
        return createToken(user, "refresh", refreshTokenExpirationSeconds);
    }

    public TokenClaims validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    public TokenClaims validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    private TokenClaims validateToken(String token, String expectedType) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format.");
            }
            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!header.contains("\"alg\":\"HS256\"")) {
                throw new IllegalArgumentException("Unsupported token algorithm.");
            }
            byte[] suppliedSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
                throw new IllegalArgumentException("Invalid token signature.");
            }
            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
            String email = payload.get("sub").asString();
            long userId = payload.get("userId").asLong();
            String type = payload.get("type").asString();
            long expiresAt = payload.get("exp").asLong();
            if (!expectedType.equals(type) || expiresAt <= Instant.now().getEpochSecond()
                    || email.isBlank() || userId <= 0) {
                throw new IllegalArgumentException("Expired or invalid token.");
            }
            return new TokenClaims(userId, email);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token.", exception);
        }
    }

    public record TokenClaims(long userId, String email) {
    }

    private String createToken(UserAccount user, String tokenType, long expirationSeconds) {
        long issuedAt = Instant.now().getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + escapeJson(user.getEmail()) + "\",\"userId\":"
                + user.getId() + ",\"type\":\"" + tokenType + "\",\"iat\":" + issuedAt
                + ",\"exp\":" + (issuedAt + expirationSeconds) + "}");
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + BASE64_URL_ENCODER.encodeToString(sign(unsignedToken));
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign authentication token.", exception);
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\b", "\\b").replace("\f", "\\f")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
