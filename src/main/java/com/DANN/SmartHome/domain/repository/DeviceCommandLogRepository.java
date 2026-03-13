package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.DeviceCommandLog;
import com.DANN.SmartHome.domain.enums.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceCommandLogRepository extends JpaRepository<DeviceCommandLog, UUID> {
    Page<DeviceCommandLog> findByDeviceTypeOrderByPublishedAtDesc(DeviceType deviceType, Pageable pageable);
}
