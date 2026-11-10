package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.FindIdRequest;
import com.teamnative.moss.domain.auth.dto.FindIdResponse;
import com.teamnative.moss.domain.auth.entity.IdLookupAttempt;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.IdLookupAttemptRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FindIdService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 30;

    private final UserRepository userRepository;
    private final IdLookupAttemptRepository attemptRepository;
    private final IdLookupAttemptInitializer attemptInitializer;
    private final String hashSecret;

    public FindIdService(UserRepository userRepository,
                         IdLookupAttemptRepository attemptRepository,
                         IdLookupAttemptInitializer attemptInitializer,
                         @Value("${jwt.secret}") String hashSecret) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.attemptInitializer = attemptInitializer;
        this.hashSecret = hashSecret;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public FindIdResponse findId(FindIdRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String lookupKey = hashPhoneNumber(request.number());
        try {
            attemptInitializer.ensureExists(lookupKey);
        } catch (DataIntegrityViolationException exception) {
            if (!attemptRepository.existsById(lookupKey)) {
                throw exception;
            }
        }

        IdLookupAttempt attempt = attemptRepository.findByLookupKeyForUpdate(lookupKey)
                .orElseThrow(() -> new IllegalStateException("아이디 조회 시도 정보를 초기화하지 못했습니다."));
        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "아이디 찾기 시도가 5회 실패하여 30분간 조회할 수 없습니다.");
        }
        attempt.clearExpiredLock(now);

        UserAccount user = userRepository.findByNameAndNumber(request.name().trim(), request.number().trim())
                .orElse(null);
        if (user == null) {
            attempt.recordFailure(now, MAX_FAILED_ATTEMPTS, LOCK_MINUTES);
            if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "아이디 찾기 시도가 5회 실패하여 30분간 조회할 수 없습니다.");
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "입력한 정보와 일치하는 계정을 찾을 수 없습니다.");
        }

        attempt.reset(now);
        return new FindIdResponse(maskEmail(user.getEmail()), user.getCreatedAt().toLocalDate());
    }

    private String maskEmail(String email) {
        int atIndex = email == null ? -1 : email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "***";
        }
        String localPart = email.substring(0, atIndex);
        return localPart.substring(0, Math.min(2, localPart.length())) + email.substring(atIndex);
    }

    private String hashPhoneNumber(String number) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(hmac.doFinal(number.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("아이디 조회 키를 생성할 수 없습니다.", exception);
        }
    }
}
