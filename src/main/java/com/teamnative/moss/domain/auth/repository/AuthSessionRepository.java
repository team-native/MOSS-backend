package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
}
