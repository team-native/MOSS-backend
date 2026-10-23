package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.entity.AuthSession;
import com.teamnative.moss.domain.auth.repository.AuthSessionRepository;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LogoutService {
    private final TokenService tokenService;
    private final AuthSessionRepository authSessionRepository;

    public LogoutService(TokenService tokenService, AuthSessionRepository authSessionRepository) {
        this.tokenService = tokenService;
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional
    public void logout(String accessToken) {
        TokenService.TokenClaims claims;
        try {
            claims = tokenService.validateAccessToken(accessToken);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 access token입니다.");
        }
        AuthSession session = authSessionRepository.findById(claims.sessionId())
                .filter(authSession -> authSession.getUserId() == claims.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "로그인 세션이 유효하지 않습니다."));
        session.revoke(Instant.now());
        authSessionRepository.save(session);
    }
}
