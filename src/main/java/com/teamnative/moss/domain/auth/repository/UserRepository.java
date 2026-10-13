package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);

    boolean existsByNumber(String number);
}
