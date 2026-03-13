package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.SensorLatest;
import com.DANN.SmartHome.domain.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorLatestRepository extends JpaRepository<SensorLatest, SensorType> {
}
