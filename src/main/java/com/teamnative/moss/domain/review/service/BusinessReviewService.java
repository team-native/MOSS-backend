package com.teamnative.moss.domain.review.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.place.service.BusinessPlaceAccessService;
import com.teamnative.moss.domain.review.dto.BusinessReplyRequest;
import com.teamnative.moss.domain.review.dto.BusinessReviewFilter;
import com.teamnative.moss.domain.review.dto.BusinessReviewListResponse;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.entity.Place;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessReviewService {
    private final BusinessPlaceAccessService businessPlaceAccessService;
    private final PlaceReviewRepository reviewRepository;

    public BusinessReviewService(BusinessPlaceAccessService businessPlaceAccessService,
                                 PlaceReviewRepository reviewRepository) {
        this.businessPlaceAccessService = businessPlaceAccessService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public BusinessReviewListResponse getReviews(String email, Long placeId, BusinessReviewFilter filter,
                                                int page, int size) {
        businessPlaceAccessService.requireOwnedPlace(email, placeId);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PlaceReview> result = switch (filter) {
            case ALL -> reviewRepository.findByPlace_IdAndDeletedAtIsNullAndModeratedAtIsNullOrderByCreatedAtDesc(placeId, pageRequest);
            case UNANSWERED -> reviewRepository.findByPlace_IdAndDeletedAtIsNullAndModeratedAtIsNullAndBusinessReplyIsNullOrderByCreatedAtDesc(
                    placeId, pageRequest);
            case ANSWERED -> reviewRepository.findByPlace_IdAndDeletedAtIsNullAndModeratedAtIsNullAndBusinessReplyIsNotNullOrderByCreatedAtDesc(
                    placeId, pageRequest);
        };
        return new BusinessReviewListResponse(placeId.toString(),
                result.getContent().stream().map(BusinessReviewListResponse.Review::from).toList(),
                page, size, result.hasNext());
    }

    @Transactional
    public BusinessReviewListResponse.Reply addReply(String email, Long reviewId, BusinessReplyRequest request) {
        PlaceReview review = findReviewForUpdate(reviewId);
        Place ownedPlace = businessPlaceAccessService.requireOwnedPlace(email, review.getPlace());
        UserAccount business = ownedPlace.getOwner();
        if (review.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }
        if (review.getBusinessReply() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 답글이 등록된 리뷰입니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        review.addBusinessReply(business, request.content().trim(), now);
        return BusinessReviewListResponse.Review.from(review).reply();
    }

    @Transactional
    public BusinessReviewListResponse.Reply updateReply(String email, Long reviewId,
                                                        BusinessReplyRequest request) {
        PlaceReview review = findReviewForUpdate(reviewId);
        businessPlaceAccessService.requireOwnedPlace(email, review.getPlace());
        UserAccount business = review.getPlace().getOwner();
        requireReplyOwner(review, business);
        ensureReplyEditable(review);
        review.updateBusinessReply(request.content().trim(), LocalDateTime.now());
        return BusinessReviewListResponse.Review.from(review).reply();
    }

    @Transactional
    public void deleteReply(String email, Long reviewId) {
        PlaceReview review = findReviewForUpdate(reviewId);
        businessPlaceAccessService.requireOwnedPlace(email, review.getPlace());
        UserAccount business = review.getPlace().getOwner();
        requireReplyOwner(review, business);
        ensureReplyEditable(review);
        review.deleteBusinessReply();
    }

    private PlaceReview findReviewForUpdate(Long reviewId) {
        return reviewRepository.findByIdForBusinessUpdate(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
    }

    private void requireReplyOwner(PlaceReview review, UserAccount business) {
        if (review.getBusinessReply() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "등록된 답글이 없습니다.");
        }
        if (!review.getBusinessReplyAuthor().getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "답글을 작성한 업주만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private void ensureReplyEditable(PlaceReview review) {
        if (LocalDateTime.now().isAfter(review.getBusinessReplyCreatedAt().plusDays(3))) {
            throw new ResponseStatusException(HttpStatus.GONE, "답글 수정·삭제 가능 기간이 지났습니다.");
        }
    }
}
