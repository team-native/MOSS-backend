package com.teamnative.moss.domain.notification.service;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.notification.dto.PushDeviceRequest;
import com.teamnative.moss.domain.notification.entity.PushDevice;
import com.teamnative.moss.domain.notification.repository.PushDeviceRepository;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PushDeviceService {
    private final UserRepository userRepository;
    private final PushDeviceRepository pushDeviceRepository;

    public PushDeviceService(UserRepository userRepository, PushDeviceRepository pushDeviceRepository) {
        this.userRepository = userRepository;
        this.pushDeviceRepository = pushDeviceRepository;
    }

    @Transactional
    public void register(String email, PushDeviceRequest request) {
        UserAccount user = findUser(email);
        String token = request.token().trim();
        String tokenHash = hash(token);
        PushDevice device = pushDeviceRepository.findByTokenHashForUpdate(tokenHash).orElse(null);
        if (device == null) {
            pushDeviceRepository.saveAndFlush(new PushDevice(user, token, request.platform(), LocalDateTime.now()));
        } else {
            device.update(user, token, request.platform(), LocalDateTime.now());
        }
    }

    @Transactional
    public void unregister(String email, PushDeviceRequest request) {
        UserAccount user = findUser(email);
        PushDevice device = pushDeviceRepository.findByTokenHashAndUser_Id(hash(request.token().trim()), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "등록된 기기를 찾을 수 없습니다."));
        pushDeviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public List<String> tokensForUser(Long userId) {
        return pushDeviceRepository.findByUser_Id(userId).stream().map(PushDevice::getToken).toList();
    }

    @Transactional
    public void removeInvalidToken(String token) {
        pushDeviceRepository.findByTokenHashForUpdate(hash(token)).ifPresent(pushDeviceRepository::delete);
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
