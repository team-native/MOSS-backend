package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.StudentSignupRequest;
import com.teamnative.moss.domain.auth.dto.SignupResponse;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentSignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentSignupService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(StudentSignupRequest request) {
        if (request.role() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "학생 회원가입에서는 가입 유형을 STUDENT로 입력해야 합니다."
            );
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 가입된 이메일입니다."
            );
        }
        if (userRepository.existsByNumber(request.number())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 가입된 휴대폰 번호입니다."
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());
        UserAccount user = new UserAccount(
                request.role(),
                request.email(),
                request.name(),
                request.number(),
                passwordHash,
                request.grade()
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
