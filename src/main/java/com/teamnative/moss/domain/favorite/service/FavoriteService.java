package com.teamnative.moss.domain.favorite.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.favorite.dto.FavoriteListResponse;
import com.teamnative.moss.domain.favorite.dto.FavoriteMutationResponse;
import com.teamnative.moss.domain.favorite.entity.Favorite;
import com.teamnative.moss.domain.favorite.repository.FavoriteRepository;
import com.teamnative.moss.domain.search.entity.Place;
import com.teamnative.moss.domain.search.repository.PlaceRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {
    private static final int MAX_FAVORITES = 30;
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, PlaceRepository placeRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public FavoriteListResponse getFavorites(String email, double latitude, double longitude) {
        UserAccount user = findUser(email);
        List<FavoriteListResponse.Place> places = favoriteRepository.findByUser_IdAndRemovedAtIsNull(user.getId())
                .stream()
                .map(favorite -> toResponse(favorite.getPlace(), latitude, longitude))
                .sorted(Comparator.comparingInt(FavoriteListResponse.Place::distanceMeters))
                .toList();
        return new FavoriteListResponse(places);
    }

    @Transactional
    public FavoriteMutationResponse addFavorite(String email, Long placeId) {
        UserAccount user = lockUser(email);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."));
        if (!place.isPubliclyVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개된 장소만 찜할 수 있습니다.");
        }

        Favorite existing = favoriteRepository.findByUserAndPlaceForUpdate(user.getId(), placeId).orElse(null);
        if (existing != null && existing.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 찜한 장소입니다.");
        }

        long activeCount = favoriteRepository.countByUser_IdAndRemovedAtIsNull(user.getId());
        if (activeCount >= MAX_FAVORITES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "찜은 최대 30개까지 저장할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            favoriteRepository.save(new Favorite(user, place, now));
        } else {
            existing.reactivate(now);
        }
        return new FavoriteMutationResponse(placeId.toString(), true, activeCount + 1, null);
    }

    @Transactional
    public FavoriteMutationResponse removeFavorite(String email, Long placeId) {
        UserAccount user = lockUser(email);
        Favorite favorite = favoriteRepository.findByUserAndPlaceForUpdate(user.getId(), placeId)
                .filter(Favorite::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "찜 목록에서 장소를 찾을 수 없습니다."));
        LocalDateTime undoExpiresAt = favorite.remove(LocalDateTime.now());
        long activeCount = favoriteRepository.countByUser_IdAndRemovedAtIsNull(user.getId());
        return new FavoriteMutationResponse(placeId.toString(), false, activeCount, undoExpiresAt);
    }

    @Transactional
    public FavoriteMutationResponse restoreFavorite(String email, Long placeId) {
        UserAccount user = lockUser(email);
        Favorite favorite = favoriteRepository.findByUserAndPlaceForUpdate(user.getId(), placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "되돌릴 찜 내역이 없습니다."));
        if (favorite.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 찜 목록에 있습니다.");
        }
        if (favorite.getUndoExpiresAt() == null || LocalDateTime.now().isAfter(favorite.getUndoExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "찜 되돌리기 가능 시간이 지났습니다.");
        }
        long activeCount = favoriteRepository.countByUser_IdAndRemovedAtIsNull(user.getId());
        if (activeCount >= MAX_FAVORITES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "찜은 최대 30개까지 저장할 수 있습니다.");
        }
        favorite.restore(LocalDateTime.now());
        return new FavoriteMutationResponse(placeId.toString(), true, activeCount + 1, null);
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
    }

    private UserAccount lockUser(String email) {
        return userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
    }

    private FavoriteListResponse.Place toResponse(Place place, double latitude, double longitude) {
        int distance = (int) Math.round(distanceMeters(latitude, longitude, place.getLatitude(), place.getLongitude()));
        return new FavoriteListResponse.Place(place.getId().toString(), place.getName(), place.getAddress(),
                place.getPlaceType().name(), place.getLatitude(), place.getLongitude(), place.getHourlyPrice(),
                place.getNoiseLevel().name(), !place.isPubliclyVisible(), distance);
    }

    private static double distanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double latDelta = Math.toRadians(latitude2 - latitude1);
        double lonDelta = Math.toRadians(longitude2 - longitude1);
        double sinLat = Math.sin(latDelta / 2);
        double sinLon = Math.sin(lonDelta / 2);
        double a = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
