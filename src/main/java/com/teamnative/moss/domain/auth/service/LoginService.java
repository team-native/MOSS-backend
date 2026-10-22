package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.LoginRequest;
import com.teamnative.moss.domain.auth.dto.LoginResponse;
import com.teamnative.moss.domain.auth.dto.RefreshTokenRequest;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "이메일 또는 비밀번호가 일치하지 않습니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return createLoginResponse(user);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다."));
        return createLoginResponse(user);
    }

    private LoginResponse createLoginResponse(UserAccount user) {
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                tokenService.createAccessToken(user),
                tokenService.createRefreshToken(user),
                "Bearer"
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CREDENTIALS_MESSAGE);
    }
}
