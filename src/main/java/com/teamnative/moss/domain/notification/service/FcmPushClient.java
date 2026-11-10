package com.teamnative.moss.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FcmPushClient {
    private static final Logger log = LoggerFactory.getLogger(FcmPushClient.class);
    private static final String OAUTH_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String JWT_BEARER_GRANT = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String projectId;
    private final String serviceAccountJson;
    private final AtomicReference<CachedAccessToken> cachedAccessToken = new AtomicReference<>();

    public FcmPushClient(ObjectMapper objectMapper,
                         @Value("${app.push.fcm.project-id:}") String projectId,
                         @Value("${app.push.fcm.service-account-json:}") String serviceAccountJson) {
        this.objectMapper = objectMapper;
        this.projectId = projectId;
        this.serviceAccountJson = serviceAccountJson;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(3));
        requestFactory.setReadTimeout(java.time.Duration.ofSeconds(8));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public boolean isConfigured() {
        return !projectId.isBlank() && !serviceAccountJson.isBlank();
    }

    public SendResult send(String token, String title, String body, Long notificationId, String targetPath) {
        if (!isConfigured()) {
            return SendResult.NOT_SENT;
        }
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("token", token);
            message.put("notification", Map.of("title", title, "body", body));
            Map<String, String> data = new LinkedHashMap<>();
            data.put("notificationId", notificationId.toString());
            if (targetPath != null) {
                data.put("targetPath", targetPath);
            }
            message.put("data", data);
            Map<String, Object> payload = Map.of("message", message);

            FcmResponse response = restClient.post()
                    .uri("https://fcm.googleapis.com/v1/projects/{projectId}/messages:send", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken())
                    .body(payload)
                    .exchange((request, fcmResponse) -> new FcmResponse(fcmResponse.getStatusCode(),
                            StreamUtils.copyToString(fcmResponse.getBody(), StandardCharsets.UTF_8)));
            if (response.status().is2xxSuccessful()) {
                return SendResult.SENT;
            }
            if (isUnregistered(response.body())) {
                return SendResult.INVALID_TOKEN;
            }
            log.warn("FCM notification delivery failed with status {}.", response.status().value());
            return SendResult.NOT_SENT;
        } catch (RestClientResponseException exception) {
            if (isUnregistered(exception.getResponseBodyAsString())) {
                return SendResult.INVALID_TOKEN;
            }
            log.warn("FCM notification delivery failed with status {}.", exception.getStatusCode().value());
            return SendResult.NOT_SENT;
        } catch (Exception exception) {
            log.warn("FCM notification delivery failed: {}", exception.getClass().getSimpleName());
            return SendResult.NOT_SENT;
        }
    }

    private String accessToken() throws IOException, GeneralSecurityException {
        Instant now = Instant.now();
        CachedAccessToken current = cachedAccessToken.get();
        if (current != null && current.expiresAt().isAfter(now)) {
            return current.value();
        }
        synchronized (cachedAccessToken) {
            current = cachedAccessToken.get();
            now = Instant.now();
            if (current != null && current.expiresAt().isAfter(now)) {
                return current.value();
            }
            JsonNode credentials = objectMapper.readTree(serviceAccountJson);
            String clientEmail = requiredCredential(credentials, "client_email");
            String privateKey = requiredCredential(credentials, "private_key");
            String assertion = createSignedAssertion(clientEmail, privateKey, now);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", JWT_BEARER_GRANT);
            form.add("assertion", assertion);
            JsonNode response = restClient.post()
                    .uri(OAUTH_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || response.path("access_token").asText().isBlank()) {
                throw new IllegalStateException("Google OAuth token response is invalid.");
            }
            long expiresIn = response.path("expires_in").asLong(3600);
            CachedAccessToken refreshed = new CachedAccessToken(response.path("access_token").asText(),
                    now.plus(Math.max(60, expiresIn - 60), ChronoUnit.SECONDS));
            cachedAccessToken.set(refreshed);
            return refreshed.value();
        }
    }

    private String createSignedAssertion(String clientEmail, String privateKeyPem, Instant now)
            throws JsonProcessingException, GeneralSecurityException {
        Map<String, String> header = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
                "iss", clientEmail,
                "scope", FCM_SCOPE,
                "aud", OAUTH_TOKEN_URL,
                "iat", now.getEpochSecond(),
                "exp", now.plus(1, ChronoUnit.HOURS).getEpochSecond());
        String encodedHeader = encodeJson(header);
        String encodedClaims = encodeJson(claims);
        String unsignedAssertion = encodedHeader + "." + encodedClaims;

        String encodedKey = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        RSAPrivateKey key = (RSAPrivateKey) java.security.KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(unsignedAssertion.getBytes(StandardCharsets.US_ASCII));
        return unsignedAssertion + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
    }

    private String encodeJson(Object value) throws JsonProcessingException {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String requiredCredential(JsonNode credentials, String name) {
        String value = credentials.path(name).asText();
        if (value.isBlank()) {
            throw new IllegalStateException("FCM service account is missing " + name + ".");
        }
        return value;
    }

    private boolean isUnregistered(String responseBody) {
        try {
            JsonNode details = objectMapper.readTree(responseBody).path("error").path("details");
            if (details.isArray()) {
                for (JsonNode detail : details) {
                    if ("UNREGISTERED".equals(detail.path("errorCode").asText())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public enum SendResult {
        SENT,
        INVALID_TOKEN,
        NOT_SENT
    }

    private record FcmResponse(HttpStatusCode status, String body) {}
    private record CachedAccessToken(String value, Instant expiresAt) {}
}
