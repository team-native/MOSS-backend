package com.teamnative.moss.domain.review.service;

import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final UserRepository userRepository;

    public ReviewService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Object> getMyReviews(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 리뷰 목록을 조회할 수 있습니다.");
        }

        // TODO 리뷰 도메인 구현 후, 로그인한 사용자가 작성한 리뷰를 조회해 반환합니다.
        return List.of();
    }
}
