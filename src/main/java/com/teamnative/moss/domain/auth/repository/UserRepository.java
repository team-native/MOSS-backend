package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);

    boolean existsByNumber(String number);

    Optional<UserAccount> findByEmail(String email);
}
