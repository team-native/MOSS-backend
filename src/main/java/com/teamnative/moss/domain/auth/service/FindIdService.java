package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.FindIdRequest;
import com.teamnative.moss.domain.auth.dto.FindIdResponse;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FindIdService {
    private final UserRepository userRepository;

    public FindIdService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public FindIdResponse findId(FindIdRequest request) {
        UserAccount user = userRepository.findByNameAndNumber(request.name(), request.number())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "입력한 정보와 일치하는 계정을 찾을 수 없습니다."));
        return new FindIdResponse(user.getEmail(), user.getCreatedAt().toLocalDate());
    }
}
