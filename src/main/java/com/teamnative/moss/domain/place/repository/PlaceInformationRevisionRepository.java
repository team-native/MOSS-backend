package com.teamnative.moss.domain.place.repository;

import com.teamnative.moss.domain.place.entity.PlaceInformationRevision;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceInformationRevisionRepository extends JpaRepository<PlaceInformationRevision, Long> {
    @Modifying
    @Query("delete from PlaceInformationRevision revision where revision.changedBy.id = :userId")
    int deleteAllByChangedById(@Param("userId") Long userId);
}
