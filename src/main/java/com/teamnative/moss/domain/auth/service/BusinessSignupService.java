package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.BusinessSignupRequest;
import com.teamnative.moss.domain.auth.dto.SignupResponse;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessSignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BusinessSignupService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(BusinessSignupRequest request) {
        if (request.role() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업주 회원가입에서는 가입 유형을 BUSINESS로 입력해야 합니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNumber(request.number())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다.");
        }

        UserAccount user = new UserAccount(
                request.role(),
                request.email(),
                request.name(),
                request.number(),
                passwordEncoder.encode(request.password()),
                null
        );
        UserAccount savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt().toLocalDate()
        );
    }
}
