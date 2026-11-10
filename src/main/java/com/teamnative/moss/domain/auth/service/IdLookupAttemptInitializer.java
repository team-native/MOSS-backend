package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.entity.IdLookupAttempt;
import com.teamnative.moss.domain.auth.repository.IdLookupAttemptRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdLookupAttemptInitializer {
    private final IdLookupAttemptRepository attemptRepository;

    public IdLookupAttemptInitializer(IdLookupAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureExists(String lookupKey) {
        if (!attemptRepository.existsById(lookupKey)) {
            attemptRepository.saveAndFlush(new IdLookupAttempt(lookupKey, LocalDateTime.now()));
        }
    }
}
