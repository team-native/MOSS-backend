package com.teamnative.moss.domain.place.service;

import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.place.dto.PlacePhotoListResponse;
import com.teamnative.moss.domain.place.dto.PlacePhotoMetadata;
import com.teamnative.moss.domain.place.dto.PlacePhotoResponse;
import com.teamnative.moss.domain.place.entity.PlacePhoto;
import com.teamnative.moss.domain.place.repository.PlacePhotoRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlacePhotoService {
    private static final long MAX_PHOTO_BYTES = 10L * 1024 * 1024;

    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final PlaceRepository placeRepository;
    private final PlacePhotoRepository photoRepository;

    public PlacePhotoService(UserRepository userRepository,
                             BusinessVerificationRepository verificationRepository,
                             PlaceRepository placeRepository,
                             PlacePhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.placeRepository = placeRepository;
        this.photoRepository = photoRepository;
    }

    @Transactional(readOnly = true)
    public PlacePhotoListResponse getBusinessPhotos(String email, Long placeId) {
        requireApprovedBusiness(email);
        Place place = requirePlace(placeId);
        List<PlacePhotoMetadata> photos = photoRepository.findMetadataByPlaceId(placeId);
        PlacePhotoResponse representative = photos.stream().filter(PlacePhotoMetadata::representative)
                .findFirst().map(PlacePhotoResponse::from).orElse(null);
        List<PlacePhotoResponse> gallery = photos.stream().filter(photo -> !photo.representative())
                .map(PlacePhotoResponse::from).toList();
        return new PlacePhotoListResponse(placeId.toString(), place.isPubliclyVisible(), representative, gallery);
    }

    @Transactional
    public PlacePhotoResponse replaceRepresentative(String email, Long placeId, MultipartFile file) {
        requireApprovedBusiness(email);
        Place place = requirePlaceForUpdate(placeId);
        ValidatedPhoto photo = validate(file);
        PlacePhoto existing = photoRepository.findByPlace_IdAndRepresentativeTrue(placeId).orElse(null);
        if (existing == null) {
            existing = new PlacePhoto(place, photo.filename(), photo.contentType(), photo.data(), true, 0,
                    LocalDateTime.now());
        } else {
            existing.replaceImage(photo.filename(), photo.contentType(), photo.data());
        }
        return PlacePhotoResponse.from(photoRepository.save(existing));
    }

    @Transactional
    public PlacePhotoResponse addPhoto(String email, Long placeId, MultipartFile file) {
        requireApprovedBusiness(email);
        Place place = requirePlaceForUpdate(placeId);
        if (photoRepository.countByPlace_IdAndRepresentativeFalse(placeId) >= 10) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "매장 사진은 최대 10장까지 등록할 수 있습니다.");
        }
        ValidatedPhoto photo = validate(file);
        int sortOrder = photoRepository.findFirstByPlace_IdAndRepresentativeFalseOrderBySortOrderDescIdDesc(placeId)
                .map(existing -> existing.getSortOrder() + 1).orElse(0);
        PlacePhoto saved = photoRepository.save(new PlacePhoto(place, photo.filename(), photo.contentType(),
                photo.data(), false, sortOrder, LocalDateTime.now()));
        return PlacePhotoResponse.from(saved);
    }

    @Transactional
    public void deletePhoto(String email, Long placeId, Long photoId) {
        requireApprovedBusiness(email);
        boolean representative = photoRepository.isRepresentative(photoId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장 사진을 찾을 수 없습니다."));
        if (representative) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대표 사진은 교체만 할 수 있습니다.");
        }
        if (photoRepository.deleteGalleryPhoto(photoId, placeId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "매장 사진을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public boolean updateVisibility(String email, Long placeId, boolean publiclyVisible) {
        requireApprovedBusiness(email);
        Place place = requirePlaceForUpdate(placeId);
        place.changeVisibility(publiclyVisible);
        return place.isPubliclyVisible();
    }

    @Transactional(readOnly = true)
    public PlacePhotoListResponse getPublicPhotos(Long placeId) {
        Place place = requirePlace(placeId);
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다.");
        }
        return getBusinessPhotosForPublic(place);
    }

    @Transactional(readOnly = true)
    public PhotoData getPublicPhoto(Long placeId, Long photoId) {
        Place place = requirePlace(placeId);
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소를 찾을 수 없습니다.");
        }
        PlacePhoto photo = photoRepository.findByIdAndPlace_Id(photoId, placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장 사진을 찾을 수 없습니다."));
        return new PhotoData(photo.getContentType(), photo.getImageData());
    }

    private PlacePhotoListResponse getBusinessPhotosForPublic(Place place) {
        List<PlacePhotoMetadata> photos = photoRepository.findMetadataByPlaceId(place.getId());
        PlacePhotoResponse representative = photos.stream().filter(PlacePhotoMetadata::representative)
                .findFirst().map(PlacePhotoResponse::from).orElse(null);
        List<PlacePhotoResponse> gallery = photos.stream().filter(photo -> !photo.representative())
                .map(PlacePhotoResponse::from).toList();
        return new PlacePhotoListResponse(place.getId().toString(), true, representative, gallery);
    }

    private Place requirePlace(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
    }

    private Place requirePlaceForUpdate(Long placeId) {
        return placeRepository.findByIdForVisibilityUpdate(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
    }

    private UserAccount requireApprovedBusiness(String email) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "업주 계정만 이용할 수 있습니다.");
        }
        boolean approved = verificationRepository.findByUser_Id(user.getId())
                .map(verification -> verification.getStatus() == BusinessVerificationStatus.APPROVED)
                .orElse(false);
        if (!approved) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사업자 인증 승인 후 이용할 수 있습니다.");
        }
        return user;
    }

    private ValidatedPhoto validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진 파일을 선택해 주세요.");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 장당 10MB 이하여야 합니다.");
        }
        String filename = sanitizeFilename(file.getOriginalFilename());
        try {
            byte[] data = file.getBytes();
            String contentType = detectImageType(filename, data);
            return new ValidatedPhoto(filename, contentType, data);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진 파일을 읽을 수 없습니다.");
        }
    }

    private String detectImageType(String filename, byte[] data) {
        String lowerName = filename.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8
                    && (data[2] & 0xFF) == 0xFF) return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            if (startsWith(data, new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10})) return "image/png";
        } else if (lowerName.endsWith(".heic")) {
            if (isHeic(data)) return "image/heic";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 JPG, PNG, HEIC 형식이어야 합니다.");
    }

    private boolean isHeic(byte[] data) {
        if (data.length < 12 || data[4] != 'f' || data[5] != 't' || data[6] != 'y' || data[7] != 'p') return false;
        String brand = new String(data, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
        return brand.equals("heic") || brand.equals("heix") || brand.equals("hevc") || brand.equals("hevx");
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (value[i] != prefix[i]) return false;
        return true;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "photo";
        String normalized = filename.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    private record ValidatedPhoto(String filename, String contentType, byte[] data) {}

    public record PhotoData(String contentType, byte[] data) {}
}
