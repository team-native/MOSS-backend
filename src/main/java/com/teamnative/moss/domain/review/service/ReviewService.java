package com.teamnative.moss.domain.review.service;

import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.review.dto.ReviewRequest;
import com.teamnative.moss.domain.review.dto.ReviewResponse;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PlaceReviewRepository reviewRepository;

    public ReviewService(UserRepository userRepository, PlaceRepository placeRepository,
                         PlaceReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(String email) {
        UserAccount user = findStudent(email);
        return reviewRepository.findByAuthor_IdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId())
                .stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public ReviewResponse createReview(String email, Long placeId, ReviewRequest request) {
        UserAccount user = findStudent(email);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."));
        BigDecimal rating = validateAndNormalize(request);
        LocalDateTime now = LocalDateTime.now();

        Optional<PlaceReview> previous = reviewRepository.findByAuthor_IdAndPlace_Id(user.getId(), placeId);
        if (previous.isPresent()) {
            PlaceReview existing = previous.get();
            if (!existing.isDeleted() || existing.canRestoreAt(now)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "장소당 리뷰는 한 개만 작성할 수 있습니다. 삭제한 리뷰는 복구할 수 있습니다.");
            }
            reviewRepository.delete(existing);
            reviewRepository.flush();
        }

        PlaceReview review = new PlaceReview(user, place, rating, request.noiseLevel(),
                normalizeContent(request.content()), normalizePhotos(request.photoUrls()), now);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request) {
        UserAccount user = findStudent(email);
        PlaceReview review = reviewRepository.findByIdAndAuthorForUpdate(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (review.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.GONE, "삭제된 리뷰는 수정할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (review.getLastEditedAt() != null && now.isBefore(review.getLastEditedAt().plusDays(1))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "리뷰는 수정 후 다음 날부터 다시 수정할 수 있습니다.");
        }
        BigDecimal rating = validateAndNormalize(request);
        review.update(rating, request.noiseLevel(), normalizeContent(request.content()),
                normalizePhotos(request.photoUrls()), now);
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse deleteReview(String email, Long reviewId) {
        UserAccount user = findStudent(email);
        PlaceReview review = reviewRepository.findByIdAndAuthorForUpdate(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (review.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 삭제된 리뷰입니다.");
        }
        review.softDelete(LocalDateTime.now());
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse restoreReview(String email, Long reviewId) {
        UserAccount user = findStudent(email);
        PlaceReview review = reviewRepository.findByIdAndAuthorForUpdate(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!review.canRestoreAt(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "리뷰 복구 가능 기간인 7일이 지났습니다.");
        }
        review.restore(LocalDateTime.now());
        return ReviewResponse.from(review);
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void permanentlyDeleteExpiredReviews() {
        List<PlaceReview> expired = reviewRepository.findByDeletedAtIsNotNullAndRestoreUntilLessThanEqual(
                LocalDateTime.now());
        reviewRepository.deleteAll(expired);
    }

    private UserAccount findStudent(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 회원만 리뷰를 작성하고 관리할 수 있습니다.");
        }
        return user;
    }

    private BigDecimal validateAndNormalize(ReviewRequest request) {
        BigDecimal rating = request.rating().stripTrailingZeros();
        if (rating.remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "별점은 0.5 단위로 입력해 주세요.");
        }
        normalizeContent(request.content());
        normalizePhotos(request.photoUrls());
        return rating;
    }

    private String normalizeContent(String content) {
        String normalized = content.trim();
        int length = normalized.codePointCount(0, normalized.length());
        if (length < 20 || length > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리뷰는 20자 이상 500자 이하여야 합니다.");
        }
        return normalized;
    }

    private List<String> normalizePhotos(List<String> photoUrls) {
        if (photoUrls == null || photoUrls.isEmpty()) return List.of();
        if (photoUrls.size() > 3 || photoUrls.stream().anyMatch(url -> url == null || url.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 주소가 비어 있지 않은 항목으로 최대 3장까지 첨부할 수 있습니다.");
        }
        return photoUrls.stream().map(String::trim).toList();
    }
}
