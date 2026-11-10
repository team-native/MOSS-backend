package com.teamnative.moss.domain.search.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.search.dto.RecentSearchResponse;
import com.teamnative.moss.domain.search.entity.RecentSearch;
import com.teamnative.moss.domain.search.repository.RecentSearchRepository;
import com.teamnative.moss.global.exception.DomainException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecentSearchService {
    private static final int MAX_RECENT_SEARCHES = 10;

    private final UserRepository userRepository;
    private final RecentSearchRepository recentSearchRepository;

    public RecentSearchService(UserRepository userRepository, RecentSearchRepository recentSearchRepository) {
        this.userRepository = userRepository;
        this.recentSearchRepository = recentSearchRepository;
    }

    @Transactional
    public void record(String email, String query) {
        UserAccount user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new DomainException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        recentSearchRepository.findByUser_IdAndQueryIgnoreCase(user.getId(), query)
                .ifPresentOrElse(RecentSearch::touch, () -> recentSearchRepository.save(
                        new RecentSearch(user, query)));
        List<RecentSearch> searches = recentSearchRepository
                .findByUser_IdOrderByCreatedAtDescIdDesc(user.getId());
        if (searches.size() > MAX_RECENT_SEARCHES) {
            recentSearchRepository.deleteAll(searches.subList(MAX_RECENT_SEARCHES, searches.size()));
        }
    }

    @Transactional(readOnly = true)
    public List<RecentSearchResponse> getRecentSearches(String email) {
        UserAccount user = findUser(email);
        return recentSearchRepository.findByUser_IdOrderByCreatedAtDescIdDesc(user.getId()).stream()
                .map(RecentSearchResponse::from).toList();
    }

    @Transactional
    public void deleteRecentSearch(String email, Long id) {
        UserAccount user = findUser(email);
        RecentSearch search = recentSearchRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "최근 검색어를 찾을 수 없습니다."));
        recentSearchRepository.delete(search);
    }

    @Transactional
    public void deleteAllRecentSearches(String email) {
        UserAccount user = findUser(email);
        recentSearchRepository.deleteByUser_Id(user.getId());
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }
}
