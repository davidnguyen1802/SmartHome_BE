package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.DeviceStateEntity;
import com.DANN.SmartHome.domain.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceStateRepository extends JpaRepository<DeviceStateEntity, DeviceType> {
}
