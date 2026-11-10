package com.teamnative.moss.domain.search.repository;

import com.teamnative.moss.domain.search.entity.RecentSearch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {
    List<RecentSearch> findByUser_IdOrderByCreatedAtDescIdDesc(Long userId);

    Optional<RecentSearch> findByUser_IdAndQueryIgnoreCase(Long userId, String query);

    Optional<RecentSearch> findByIdAndUser_Id(Long id, Long userId);

    void deleteByUser_Id(Long userId);
}
