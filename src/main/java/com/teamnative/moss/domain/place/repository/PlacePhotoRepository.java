package com.teamnative.moss.domain.place.repository;

import com.teamnative.moss.domain.place.entity.PlacePhoto;
import com.teamnative.moss.domain.place.dto.PlacePhotoMetadata;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacePhotoRepository extends JpaRepository<PlacePhoto, Long> {
    @Query("select new com.teamnative.moss.domain.place.dto.PlacePhotoMetadata(photo.id, photo.place.id, "
            + "photo.originalFilename, photo.contentType, photo.representative, photo.sortOrder, photo.createdAt) "
            + "from PlacePhoto photo where photo.place.id = :placeId "
            + "order by photo.representative desc, photo.sortOrder asc, photo.id asc")
    List<PlacePhotoMetadata> findMetadataByPlaceId(@Param("placeId") Long placeId);

    Optional<PlacePhoto> findByPlace_IdAndRepresentativeTrue(Long placeId);

    Optional<PlacePhoto> findByIdAndPlace_Id(Long photoId, Long placeId);

    long countByPlace_IdAndRepresentativeFalse(Long placeId);

    Optional<PlacePhoto> findFirstByPlace_IdAndRepresentativeFalseOrderBySortOrderDescIdDesc(Long placeId);

    @Query("select photo.representative from PlacePhoto photo where photo.id = :photoId and photo.place.id = :placeId")
    Optional<Boolean> isRepresentative(@Param("photoId") Long photoId, @Param("placeId") Long placeId);

    @Modifying
    @Query("delete from PlacePhoto photo where photo.id = :photoId and photo.place.id = :placeId "
            + "and photo.representative = false")
    int deleteGalleryPhoto(@Param("photoId") Long photoId, @Param("placeId") Long placeId);
}
