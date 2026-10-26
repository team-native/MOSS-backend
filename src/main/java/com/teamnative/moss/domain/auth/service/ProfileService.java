package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.ProfileResponse;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 프로필을 조회할 수 있습니다.");
        }
        return new ProfileResponse(user.getName(), user.getEmail(), user.getGrade(), 0, 0);
    }
}
