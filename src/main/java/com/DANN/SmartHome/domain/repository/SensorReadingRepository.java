package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.SensorReading;
import com.DANN.SmartHome.domain.enums.SensorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {
    Page<SensorReading> findBySensorTypeOrderByReceivedAtDesc(SensorType sensorType, Pageable pageable);
}
