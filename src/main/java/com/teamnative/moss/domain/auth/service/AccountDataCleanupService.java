package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.AuthSessionRepository;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.EmailVerificationRepository;
import com.teamnative.moss.domain.favorite.repository.FavoriteRepository;
import com.teamnative.moss.domain.notification.repository.NotificationRepository;
import com.teamnative.moss.domain.place.repository.PlaceInformationRevisionRepository;
import com.teamnative.moss.domain.report.entity.UserReport;
import com.teamnative.moss.domain.report.repository.UserReportRepository;
import com.teamnative.moss.domain.review.entity.PlaceReview;
import com.teamnative.moss.domain.review.repository.PlaceReviewRepository;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDataCleanupService {
    private final UserReportRepository reportRepository;
    private final PlaceReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationRepository notificationRepository;
    private final BusinessVerificationRepository businessVerificationRepository;
    private final PlaceInformationRevisionRepository revisionRepository;
    private final PlaceRepository placeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public AccountDataCleanupService(UserReportRepository reportRepository,
                                     PlaceReviewRepository reviewRepository,
                                     FavoriteRepository favoriteRepository,
                                     NotificationRepository notificationRepository,
                                     BusinessVerificationRepository businessVerificationRepository,
                                     PlaceInformationRevisionRepository revisionRepository,
                                     PlaceRepository placeRepository,
                                     AuthSessionRepository authSessionRepository,
                                     EmailVerificationRepository emailVerificationRepository) {
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.notificationRepository = notificationRepository;
        this.businessVerificationRepository = businessVerificationRepository;
        this.revisionRepository = revisionRepository;
        this.placeRepository = placeRepository;
        this.authSessionRepository = authSessionRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Transactional
    public void deleteUserData(UserAccount user) {
        Long userId = user.getId();

        Map<Long, UserReport> reports = new LinkedHashMap<>();
        reportRepository.findByReporter_IdOrderByCreatedAtDesc(userId)
                .forEach(report -> reports.put(report.getId(), report));
        reportRepository.findByReview_Author_Id(userId)
                .forEach(report -> reports.put(report.getId(), report));
        reportRepository.deleteAll(new ArrayList<>(reports.values()));
        reportRepository.flush();

        for (PlaceReview review : reviewRepository.findAllByBusinessReplyAuthor_Id(userId)) {
            review.deleteBusinessReply();
        }
        reviewRepository.flush();

        reviewRepository.deleteAll(reviewRepository.findAllByAuthor_Id(userId));
        reviewRepository.flush();

        placeRepository.clearOwnerByOwnerId(userId);
        favoriteRepository.deleteAllForUser(userId);
        notificationRepository.deleteAllForUser(userId);
        businessVerificationRepository.deleteByUserId(userId);
        revisionRepository.deleteAllByChangedById(userId);
        authSessionRepository.deleteAllForUser(userId);
        emailVerificationRepository.deleteById(user.getEmail());
    }
}
