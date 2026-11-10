package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.repository.AuthSessionRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountDeleteService {
    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final AccountDataCleanupService accountDataCleanupService;

    public AccountDeleteService(UserRepository userRepository,
                                AuthSessionRepository authSessionRepository,
                                AccountDataCleanupService accountDataCleanupService) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.accountDataCleanupService = accountDataCleanupService;
    }

    @Transactional
    public void requestDeletion(String email) {
        var user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다."));
        user.requestDeletion(LocalDateTime.now());
        authSessionRepository.revokeAllForUser(user.getId(), Instant.now());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        List<UserAccount> expiredAccounts = userRepository.findExpiredForDeletion(LocalDateTime.now().minusDays(30));
        for (UserAccount user : expiredAccounts) {
            accountDataCleanupService.deleteUserData(user);
            userRepository.delete(user);
        }
    }
}
