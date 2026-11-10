package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.repository.IdLookupAttemptRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdLookupAttemptCleanupService {
    private final IdLookupAttemptRepository attemptRepository;

    public IdLookupAttemptCleanupService(IdLookupAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void deleteExpiredAttempts() {
        attemptRepository.deleteOlderThan(LocalDateTime.now().minusDays(30));
    }
}
