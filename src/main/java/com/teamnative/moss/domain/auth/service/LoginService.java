package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.LoginRequest;
import com.teamnative.moss.domain.auth.dto.LoginResponse;
import com.teamnative.moss.domain.auth.dto.RefreshTokenRequest;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.entity.AuthSession;
import com.teamnative.moss.domain.auth.repository.AuthSessionRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 5;
    private static final String INVALID_CREDENTIALS_MESSAGE = "이메일 또는 비밀번호가 일치하지 않습니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthSessionRepository authSessionRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
                        AuthSessionRepository authSessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional(noRollbackFor = {ResponseStatusException.class, LoginFailureException.class})
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailForUpdate(request.email())
                .orElseThrow(() -> invalidCredentials(0));
        LocalDateTime now = LocalDateTime.now();
        if (user.isLoginLockedAt(now)) {
            throw new LoginFailureException(HttpStatus.TOO_MANY_REQUESTS,
                    "로그인 시도가 5회 실패하여 5분간 재시도할 수 없습니다.", null, null);
        }
        user.clearExpiredLoginLock(now);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin(now, MAX_FAILED_ATTEMPTS, LOCK_MINUTES);
            userRepository.save(user);
            if (user.isLoginLockedAt(now)) {
                throw new LoginFailureException(HttpStatus.TOO_MANY_REQUESTS,
                        "로그인 시도가 5회 실패하여 5분간 재시도할 수 없습니다.", null, null);
            }
            throw invalidCredentials(user.getFailedLoginAttempts());
        }

        user.resetFailedLogins();
        userRepository.save(user);
        return createLoginResponse(user, UUID.randomUUID().toString(), true);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        TokenService.TokenClaims claims;
        try {
            claims = tokenService.validateRefreshToken(request.refreshToken());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다.");
        }
        UserAccount user = userRepository.findById(claims.userId())
                .filter(account -> account.getEmail().equals(claims.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "리프레시 토큰이 유효하지 않거나 만료되었습니다."));
        return createLoginResponse(user, claims.sessionId(), false);
    }

    private LoginResponse createLoginResponse(UserAccount user, String sessionId, boolean createSession) {
        String accessToken = tokenService.createAccessToken(user, sessionId);
        String refreshToken = tokenService.createRefreshToken(user, sessionId);
        Instant refreshExpiresAt = tokenService.getExpiration(refreshToken);
        if (createSession) {
            authSessionRepository.save(new AuthSession(sessionId, user.getId(), refreshExpiresAt));
        } else {
            AuthSession session = authSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "로그인 세션이 유효하지 않습니다."));
            session.extendUntil(refreshExpiresAt);
            authSessionRepository.save(session);
        }
        return new LoginResponse(
                user.getId(), user.getName(), user.getEmail(),
                accessToken, refreshToken, "Bearer");
    }

    private LoginFailureException invalidCredentials(int failedAttempts) {
        return new LoginFailureException(HttpStatus.BAD_REQUEST, INVALID_CREDENTIALS_MESSAGE,
                failedAttempts, MAX_FAILED_ATTEMPTS);
    }
}
