package com.teamnative.moss.domain.auth.repository;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);

    boolean existsByNumber(String number);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByNameAndNumber(String name, String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from UserAccount user where user.email = :email")
    Optional<UserAccount> findByEmailForUpdate(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from UserAccount user where user.email = :email")
    Optional<UserAccount> findByEmailForPasswordUpdate(@Param("email") String email);

}
