package com.teamnative.moss.domain.notification.repository;

import com.teamnative.moss.domain.notification.entity.PushDevice;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select device from PushDevice device where device.tokenHash = :tokenHash")
    Optional<PushDevice> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<PushDevice> findByUser_Id(Long userId);

    Optional<PushDevice> findByTokenHashAndUser_Id(String tokenHash, Long userId);
}
