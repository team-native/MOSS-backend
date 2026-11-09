package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.ProfileResponse;
import com.teamnative.moss.domain.auth.dto.ProfileUpdateResponse;
import com.teamnative.moss.domain.auth.dto.UpdateProfileRequest;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final UserReportRepository userReportRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PlaceReviewRepository placeReviewRepository,
                          UserReportRepository userReportRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.placeReviewRepository = placeReviewRepository;
        this.userReportRepository = userReportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        long reviewCount = placeReviewRepository.countByAuthor_IdAndDeletedAtIsNull(user.getId());
        long reportCount = userReportRepository.countByReporter_Id(user.getId());
        return new ProfileResponse(user.getName(), user.getEmail(), user.getNumber(), user.getGrade(),
                reviewCount, reportCount, user.getSchoolName(), user.getSchoolLatitude(), user.getSchoolLongitude());
    }

    @Transactional
    public ProfileResponse setSchool(String email, String schoolName, Double latitude, Double longitude) {
        var user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        if (user.getRole() != com.teamnative.moss.domain.auth.entity.Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 학교를 설정할 수 있습니다.");
        }
        user.updateSchool(schoolName.trim(), latitude, longitude);
        return getProfile(email);
    }

    @Transactional
    public ProfileResponse clearSchool(String email) {
        var user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        if (user.getRole() != com.teamnative.moss.domain.auth.entity.Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 학교를 설정할 수 있습니다.");
        }
        user.updateSchool(null, null, null);
        return getProfile(email);
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        var user = userRepository.findByEmailForUpdate(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));

        if (request.email() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일은 변경할 수 없습니다.");
        }
        if (request.number() != null && request.number().equals(user.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 사용 중인 전화번호와 동일합니다.");
        }
        if (request.number() != null && userRepository.existsByNumberAndIdNot(request.number(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다.");
        }
        if (request.name() != null && request.name().trim().equals(user.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 사용 중인 이름과 동일합니다.");
        }
        if (request.grade() != null && user.getRole() != com.teamnative.moss.domain.auth.entity.Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 학년을 변경할 수 있습니다.");
        }
        if (request.password() != null && passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.updateProfile(request.name() == null ? null : request.name().trim(), null, request.number(), request.grade());
        if (request.password() != null) user.changePassword(passwordEncoder.encode(request.password()));
        return new ProfileUpdateResponse(user.getName(), user.getEmail(), user.getNumber(), user.getGrade());
    }
}
